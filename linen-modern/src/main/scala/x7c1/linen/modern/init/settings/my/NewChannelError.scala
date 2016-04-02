package x7c1.linen.modern.init.settings.my

sealed trait NewChannelError {
  def message: String
  def dump: String
}

case class EmptyName() extends NewChannelError {
  override def message = "name required"
  override def dump = message
}
