package x7c1.linen.repository.channel.my

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{AccountIdentifiable, ChannelDeletable, ChannelIdentifiable, ChannelRecord, ChannelStatusRecord}
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.presets.{CursorClosableSequence, CanTraverse, DefaultProvidable}
import x7c1.wheat.modern.sequence.Sequence

trait MyChannelAccessor extends Sequence[MyChannelRow]{
}

object MyChannelAccessor {
  def createForDebug(db: SQLiteDatabase, accountId: Long): MyChannelAccessor = {
    ClosableMyChannelAccessor.create(db, ClientAccount(accountId)) match {
      case Left(e) => throw e
      case Right(accessor) => accessor
    }
  }
}

trait MyChannelRecord extends TypedFields
  with ChannelRecord
  with ChannelStatusRecord

sealed trait MyChannelRow

case class MyChannel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date,
  isSubscribed: Boolean ) extends MyChannelRow

object MyChannel {
  implicit object id extends ChannelIdentifiable[MyChannel]{
    override def toId = _.channelId
  }
  implicit object deletable extends ChannelDeletable[MyChannel](_.channelId)

  implicit object traversable extends CanTraverse[AccountIdentifiable, MyChannel]{
    override def query[X: AccountIdentifiable](target: X): Query = {
      val id = implicitly[AccountIdentifiable[X]] toId target
      ClosableMyChannelAccessor createQuery ClientAccount(id)
    }
    override def fromCursor(raw: Cursor) = {
      val cursor = TypedCursor[MyChannelRecord](raw)
      val sequence = new CursorClosableSequence[MyChannel] {
        override def findAt(position: Int) = {
          (cursor moveToFind position){
            MyChannel(
              channelId = cursor._id,
              name = cursor.name,
              description = cursor.description,
              createdAt = cursor.created_at.typed,
              isSubscribed = cursor.subscribed == 1
            )
          }
        }
        override def closeCursor() = raw.close()
        override def length = raw.getCount
      }
      Right(sequence)
    }
  }
  implicit object providable extends DefaultProvidable[AccountIdentifiable, MyChannel]
}

case class MyChannelFooter() extends MyChannelRow
