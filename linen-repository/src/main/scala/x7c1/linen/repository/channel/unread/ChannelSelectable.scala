package x7c1.linen.repository.channel.unread

import x7c1.linen.database.struct.HasChannelId


trait ChannelSelectable[A] extends HasChannelId[A]{
  def nameOf: A => String
}

object ChannelSelectable {
  implicit def selectable[X, A: ChannelSelectable]: ChannelSelectable[(X, A)] =
    new ChannelSelectable[(X, A)]{
      override def nameOf = {
        case (_, a) => implicitly[ChannelSelectable[A]] nameOf a
      }
      override def toId = {
        case (_, a) => implicitly[ChannelSelectable[A]] toId a
      }
    }
}
