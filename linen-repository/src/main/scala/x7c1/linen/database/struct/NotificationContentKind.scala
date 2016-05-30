package x7c1.linen.database.struct

import x7c1.wheat.macros.database.FieldConvertible

sealed trait NotificationContentKind {
  def text: String
}

object NotificationContentKind {
  case class UnknownKind(text: String) extends NotificationContentKind

  case object SourceLoaderKind extends NotificationContentKind {
    override def text: String = "source_loader"
  }
  case object ChannelLoaderKind extends NotificationContentKind {
    override def text: String = "channel_loader"
  }
  case object PresetLoaderKind extends NotificationContentKind {
    override def text: String = "preset_loader"
  }
  implicit object convertible extends FieldConvertible[String, NotificationContentKind]{
    override def wrap(text: String) = {
      val kinds = Seq(
        SourceLoaderKind,
        ChannelLoaderKind,
        PresetLoaderKind
      )
      kinds.find(_.text == text) getOrElse UnknownKind(text)
    }
    override def unwrap(kind: NotificationContentKind) = kind.text
  }
}
