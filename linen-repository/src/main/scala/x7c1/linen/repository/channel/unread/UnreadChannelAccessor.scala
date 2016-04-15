package x7c1.linen.repository.channel.unread

import android.database.Cursor
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.channel.unread.ChannelAccessorError.UnexpectedError
import x7c1.linen.repository.channel.unread.ChannelLoaderEvent.{Done, AccessorError}
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.TaskProvider.async
import x7c1.wheat.modern.database.Query

trait UnreadChannelAccessor {
  def findAt(position: Int): Option[UnreadChannel]
  def length: Int
}

private object InternalChannelAccessor {
  def create(
    helper: DatabaseHelper,
    clientAccountId: Long ): Either[ChannelAccessorError, InternalChannelAccessor] = {

    try {
      val query = createQuery(clientAccountId)
      val raw = helper.getReadableDatabase.rawQuery(query.sql, query.selectionArgs)
      Right apply new InternalChannelAccessor(raw)
    } catch {
      case e: Exception =>
        Left apply UnexpectedError(e)
    }
  }
  def createQuery(clientAccountId: Long): Query = {
    // TODO: select only unread channels
    val sql =
      """SELECT
        | c1._id AS channel_id,
        | c1.name AS name
        |FROM channel_statuses AS c2
        |INNER JOIN channels AS c1 ON
        | c1._id = c2.channel_id
        |WHERE
        | c2.account_id = ? AND
        | c2.subscribed = 1
        |ORDER BY c1.created_at DESC
      """.stripMargin

    new Query(sql, Array(
      clientAccountId.toString
    ))
  }
}

private class InternalChannelAccessor(raw: Cursor) extends UnreadChannelAccessor {
  lazy val cursor = TypedCursor[UnreadChannelRecord](raw)

  override def findAt(position: Int) = cursor.moveToFind(position){
    UnreadChannel(cursor.channel_id, cursor.name)
  }
  override def length = raw.getCount

  def closeCursor(): Unit = raw.close()

  def headChannel: Option[UnreadChannel] = findAt(0)
}

trait UnreadChannelRecord extends TypedFields {
  def channel_id: Long
  def name: String
}

trait ChannelSelectable[A] {
  def channelIdOf: A => Long
  def nameOf: A => String
}

class UnreadChannelLoader(helper: DatabaseHelper, client: ClientAccount){
  private lazy val holder = new AccessorHolder

  lazy val accessor: UnreadChannelAccessor = holder

  def startLoading(): CallbackTask[ChannelLoaderEvent] = async {
    Log info s"[start]"
    InternalChannelAccessor.create(helper, client.accountId)
  } map {
    case Right(loadedAccessor) =>
      Log info s"[done]"
      holder updateAccessor loadedAccessor
      new Done(client, loadedAccessor.headChannel)
    case Left(error) =>
      Log info s"[failed]"
      new AccessorError(error)
  }
  private class AccessorHolder extends UnreadChannelAccessor {
    private var underlying: Option[InternalChannelAccessor] = None

    def updateAccessor(accessor: InternalChannelAccessor): Unit = synchronized {
      underlying foreach {_.closeCursor()}
      underlying = Some(accessor)
    }
    override def findAt(position: Int): Option[UnreadChannel] = {
      underlying flatMap (_ findAt position)
    }
    override def length: Int = {
      underlying map (_.length) getOrElse 0
    }
  }
}
