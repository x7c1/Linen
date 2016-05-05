package x7c1.wheat.modern.database.selector

trait CanIdentify[A]{
  type ID
  def toId: A => ID
}

trait Identifiable[A, X] extends CanIdentify[A]{
  override type ID = X
}

trait IdEndo[A]{
  self: CanIdentify[A] =>

  override def toId = identity[A] _
}
