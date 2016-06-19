package x7c1.linen.database.mixin

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasChannelStatusKey
import x7c1.linen.repository.source.unread.UnreadSourceAccessorQueries
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.SelectorProvidable
import x7c1.wheat.modern.database.selector.presets.{CanDetectBySelect, DetectFrom}

trait UnreadChannelRecord

object UnreadChannelRecord {
  implicit object providable extends SelectorProvidable[
    UnreadChannelRecord,
    UnreadChannelRecordSelector
  ]
  implicit object toDetect extends CanDetectBySelect[HasChannelStatusKey, UnreadChannelRecord] {
    override def queryAbout[X: HasChannelStatusKey](target: X): Query = {
      val key = implicitly[HasChannelStatusKey[X]] toId target
      Query(
        sql = UnreadSourceAccessorQueries.sql3 + " LIMIT 1",
        selectionArgs = Array(
          key.accountId.toString,
          key.channelId.toString
        )
      )
    }
    override def reify(cursor: Cursor): Boolean = {
      cursor.getCount > 0
    }
  }
}

class UnreadChannelRecordSelector(
  override protected val db: SQLiteDatabase
) extends DetectFrom[HasChannelStatusKey, UnreadChannelRecord]
