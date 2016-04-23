package x7c1.linen.repository.channel

trait ChannelIdentifiable[A]{
  def channelId(target: A): Long
}
