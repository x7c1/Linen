package x7c1.linen.database.struct

import x7c1.linen.database.struct.NotificationContentKind.{ChannelLoaderKind, ScheduleLoaderKind, SourceLoaderKind, UnknownKind}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.IdEndo

sealed trait NotificationKey {
  def contentKey: String
  def contentKind: NotificationContentKind
}

object NotificationKey {
  import NotificationIdRecord.column

  case class UnknownKey(contentKey: String, contentKind: UnknownKind) extends NotificationKey

  case class SourceLoaderKey[S: HasSourceId](source: S) extends NotificationKey {
    override val contentKey = {
      val sourceId = implicitly[HasSourceId[S]] toId source
      s"source:$sourceId"
    }
    override def contentKind = SourceLoaderKind
  }
  case class ChannelLoaderKey[A: HasAccountId, C: HasChannelId]
    (account: A, channel: C) extends NotificationKey {

    override val contentKey = {
      val accountId = implicitly[HasAccountId[A]] toId account
      val channelId = implicitly[HasChannelId[C]] toId channel
      s"account:$accountId,channel:$channelId"
    }
    override def contentKind = ChannelLoaderKind
  }
  case class ScheduleLoaderKey[A: HasLoaderScheduleId](schedule: A) extends NotificationKey {
    override val contentKey = {
      val scheduleId = implicitly[HasLoaderScheduleId[A]] toId schedule
      s"schedule:$scheduleId"
    }
    override def contentKind = ScheduleLoaderKind
  }
  implicit def key[A <: NotificationKey]: HasNotificationKey[A] = {
    new HasNotificationKey[A] with IdEndo[A]
  }
  implicit object insertable extends Insertable[NotificationKey]{
    override def tableName = NotificationIdRecord.table
    override def toContentValues(key: NotificationKey) =
      TypedFields.toContentValues(
        column.notification_content_key -> key.contentKey,
        column.notification_content_kind -> key.contentKind,
        column.created_at -> Date.current()
      )
  }

}
