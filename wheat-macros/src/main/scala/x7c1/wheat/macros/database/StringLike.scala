package x7c1.wheat.macros.database


trait StringLike[A]{
  def asString(target: A): String
}
object StringLike {
  implicit object string extends StringLike[String]{
    override def asString(target: String): String = target.toString
  }
  implicit object int extends StringLike[Int]{
    override def asString(target: Int): String = target.toString
  }
  implicit object double extends StringLike[Double]{
    override def asString(target: Double): String = target.toString
  }
  implicit object long extends StringLike[Long]{
    override def asString(target: Long): String = target.toString
  }
}
