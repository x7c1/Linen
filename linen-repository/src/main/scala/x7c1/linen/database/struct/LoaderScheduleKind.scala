package x7c1.linen.database.struct

import x7c1.wheat.macros.database.FieldConvertible
import x7c1.wheat.modern.database.selector.IdEndo

sealed trait LoaderScheduleKind {
  def label: String
}

object LoaderScheduleKind {

  case object AllChannels extends LoaderScheduleKind {
    override def label = "all_channels"
  }
  case object SingleChannel extends LoaderScheduleKind {
    override def label = "single_channel"
  }
  case object SingleSource extends LoaderScheduleKind {
    override def label = "single_source"
  }
  case class UnknownKind(override val label: String) extends LoaderScheduleKind

  implicit def labelable[A <: LoaderScheduleKind]: HasScheduleKindLabel[A] = {
    new HasScheduleKindLabel[A] with IdEndo[A]
  }
  implicit object convertible extends FieldConvertible[String, LoaderScheduleKind]{
    override def wrap(value: String): LoaderScheduleKind = {
      val kinds = Seq(
        AllChannels,
        SingleChannel,
        SingleSource
      )
      kinds.find(_.label == value) getOrElse UnknownKind(value)
    }
    override def unwrap(value: LoaderScheduleKind): String = value.label
  }
}