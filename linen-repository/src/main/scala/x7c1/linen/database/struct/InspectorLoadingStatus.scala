package x7c1.linen.database.struct

import x7c1.wheat.macros.database.FieldConvertible

sealed class InspectorLoadingStatus private (
  val value: Int
)

object InspectorLoadingStatus {

  case object Loading extends InspectorLoadingStatus(1)

  case object LoadingCompleted extends InspectorLoadingStatus(2)

  case object ParseError extends InspectorLoadingStatus(3)

  case object ConnectionTimeout extends InspectorLoadingStatus(4)

  case class UnknownStatus(
    override val value: Int) extends InspectorLoadingStatus(value)

  implicit object convertible extends FieldConvertible[Int, InspectorLoadingStatus]{
    val all = Seq(
      Loading,
      LoadingCompleted,
      ParseError,
      ConnectionTimeout
    )
    override def wrap(value: Int): InspectorLoadingStatus = {
      all find (_.value == value) getOrElse UnknownStatus(value)
    }
    override def unwrap(status: InspectorLoadingStatus): Int = {
      status.value
    }
  }
}
