package x7c1.linen.domain

object AccountIdentifiable {
  def apply(id: Long): AccountIdentifiable = new AccountIdentifiable {
    override def accountId: Long = id
  }
}

trait AccountIdentifiable {
  def accountId: Long
}
