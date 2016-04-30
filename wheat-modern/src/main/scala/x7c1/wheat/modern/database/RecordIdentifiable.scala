package x7c1.wheat.modern.database

trait RecordIdentifiable[A]{
  type ID
  def idOf(target: A): ID
}

trait EntityIdentifiable[A, X] extends RecordIdentifiable[A]{
  override type ID = X
}
