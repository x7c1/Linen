package x7c1.wheat.modern.database.selector

trait UnitIdentifiable[A] extends Identifiable[A, Unit]

object UnitIdentifiable {
  implicit object id extends UnitIdentifiable[Unit] with IdEndo[Unit]
}
