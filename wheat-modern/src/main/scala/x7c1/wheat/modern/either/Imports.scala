package x7c1.wheat.modern.either

object Imports {
  import scala.language.implicitConversions

  implicit class OptionWrapper[A, B](x: Either[A, B]) {
    def toOptionEither: OptionEither[A, B] = {
      x match {
        case Left(l) => OptionLeft(l)
        case Right(r) => OptionRight(Some(r))
      }
    }
  }
  implicit class OptionWrapper2[A, B](x: Either[A, Option[B]]) {
    def toOptionEither: OptionEither[A, B] = {
      x match {
        case Left(l) => OptionLeft(l)
        case Right(r) => OptionRight(r)
      }
    }
  }
}
