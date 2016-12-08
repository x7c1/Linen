package x7c1.linen.database.struct

import java.net.UnknownHostException

import x7c1.wheat.macros.database.FieldConvertible

sealed class InspectorLoadingStatus private (
  val value: Int
)

object InspectorLoadingStatus {

  case object Loading extends InspectorLoadingStatus(1)

  case object LoadingCompleted extends InspectorLoadingStatus(2)

  case object ParseError extends InspectorLoadingStatus(3)

  case object ConnectionTimeout extends InspectorLoadingStatus(4)

  case object UnknownHostError extends InspectorLoadingStatus(5)

  case object UnknownError extends InspectorLoadingStatus(6)

  case class UnknownStatus(
    override val value: Int) extends InspectorLoadingStatus(value)

  implicit object convertible extends FieldConvertible[Int, InspectorLoadingStatus]{
    val all = Seq(
      Loading,
      LoadingCompleted,
      ParseError,
      ConnectionTimeout,
      UnknownHostError,
      UnknownError
    )
    override def wrap(value: Int): InspectorLoadingStatus = {
      all find (_.value == value) getOrElse UnknownStatus(value)
    }
    override def unwrap(status: InspectorLoadingStatus): Int = {
      status.value
    }
  }

  def fromException(e: Exception): InspectorLoadingStatus = {
    e match {
      case e: UnknownHostException => UnknownHostError
      case _ => UnknownError
    }
  }
  def fromException(e: Option[Exception]): InspectorLoadingStatus = {
    e.map(fromException).getOrElse(UnknownError)
  }
}
