package x7c1.linen.repository.channel.my

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.MyChannelRecord
import x7c1.linen.repository.account.ClientAccount
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.presets.ClosableSequence
import x7c1.wheat.modern.formatter.ThrowableFormatter.format


class MyChannelAccessorLoader(db: SQLiteDatabase){

  private var accessorHolder: Option[AccessorHolder] = None

  def reload(client: ClientAccount)(f: MyChannelAccessor => Unit): Unit = {
    accessorHolder -> createAccessor(client) match {
      case (Some(holder), Right(accessor)) =>
        holder updateAccessor accessor
        f(holder)
      case (None, Right(accessor)) =>
        val holder = new AccessorHolder(accessor)
        this.accessorHolder = Some(holder)
        f(holder)
      case (_, Left(exception)) =>
        Log error format(exception){"[unexpected]"}
    }
  }
  private def createAccessor(client: ClientAccount) = {
    val internal = ClosableMyChannelAccessor.create(db, client)
    internal.right map (new SourceFooterAppender(_))
  }
  private class AccessorHolder(
    private var underlying: ClosableMyChannelAccessor) extends MyChannelAccessor {

    def updateAccessor(accessor: ClosableMyChannelAccessor) = synchronized {
      underlying.closeCursor()
      underlying = accessor
    }
    override def findAt(position: Int) = underlying findAt position

    override def length = underlying.length
  }
}

private object ClosableMyChannelAccessor {
  def create(
    db: SQLiteDatabase,
    client: ClientAccount): Either[Exception, ClosableMyChannelAccessor] = {

    try {
      val query = createQuery(client)
      val raw = db.rawQuery(query.sql, query.selectionArgs)
      Right apply new ClosableMyChannelAccessorImpl(raw, client)
    } catch {
      case e: Exception => Left(e)
    }
  }
  def createQuery(client: ClientAccount): Query = {
    MyChannelRecord.traversable.query(client)
  }
}

private trait ClosableMyChannelAccessor
  extends MyChannelAccessor
  with ClosableSequence[MyChannelRow]

private class ClosableMyChannelAccessorImpl (
  rawCursor: Cursor, client: ClientAccount) extends ClosableMyChannelAccessor {

  private lazy val cursor = TypedCursor[MyChannelRecord](rawCursor)

  override def findAt(position: Int) =
    (cursor moveToFind position){
      MyChannel(
        channelId = cursor._id,
        name = cursor.name,
        description = cursor.description,
        createdAt = cursor.created_at.typed,
        isSubscribed = cursor.subscribed == 1
      )
    }

  override def length = rawCursor.getCount

  override def closeCursor(): Unit = rawCursor.close()
}

private class SourceFooterAppender(
  accessor: ClosableMyChannelAccessor) extends ClosableMyChannelAccessor{

  override def findAt(position: Int) = {
    if (position == accessor.length){
      Some(MyChannelFooter())
    } else {
      accessor findAt position
    }
  }
  override def length: Int = {
    // +1 to append Footer
    accessor.length + 1
  }
  override def closeCursor() = accessor.closeCursor()
}
