package x7c1.wheat.modern.database

trait HasTable[A]{
  def tableName: String
}
object HasTable {
  class Where[A](name: String) extends HasTable[A]{
    override def tableName: String = name
  }
}
