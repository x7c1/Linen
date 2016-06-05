package x7c1.linen.repository.channel.unread

import x7c1.linen.database.struct.HasChannelId


trait ChannelSelectable[A] extends HasChannelId[A]{
  def nameOf: A => String
}
