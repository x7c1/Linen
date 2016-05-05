package x7c1.wheat.modern.database.selector

trait CanIdentify[A]{
  type ID
  def idOf(target: A): ID
}

trait Identifiable[A, X] extends CanIdentify[A]{
  override type ID = X
}

trait IdEndo[A]{
  self: CanIdentify[A] =>

  override def idOf(target: A) = target
}
