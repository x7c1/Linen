package x7c1.wheat.modern.either


sealed abstract class OptionEither[+L, +A] {

  def map[B](f: A => B): OptionEither[L, B] =
    this match {
      case OptionRight(r) => OptionRight(r map f)
      case OptionLeft(l) => OptionLeft(l)
    }

  def flatMap[L2 >: L, B](f: A => OptionEither[L2, B]): OptionEither[L2, B] =
    this match {
      case OptionRight(Some(r)) => f(r)
      case OptionRight(None) => OptionRight(None)
      case OptionLeft(l) => OptionLeft(l)
    }

  def option: OptionProjection[L, A] = OptionProjection(this)

  def via[C](f: Either[L, Option[A]] => C): C = f(toEither)

  def toEither: Either[L, Option[A]] = this match {
    case OptionRight(r) => Right(r)
    case OptionLeft(l) => Left(l)
  }

  def toOption: Option[A] = this match {
    case OptionRight(r) => r
    case OptionLeft(l) => None
  }
}

final case class OptionProjection[+L, +A](either: OptionEither[L, A]){

  def map[B](f: Option[A] => B): OptionEither[L, B] =
    either match {
      case OptionRight(r) => OptionRight(f(r))
      case OptionLeft(l) => OptionLeft(l)
    }

  def flatMap[L2 >: L, B](f: Option[A] => OptionEither[L2, B]): OptionEither[L2, B] =
    either match {
      case OptionRight(r) => f(r)
      case OptionLeft(l) => OptionLeft(l)
    }
}

final case class OptionRight[+L, +R]
  (r: Option[R]) extends OptionEither[L, R]

object OptionRight {
  def apply[L, R](right: R): OptionRight[L, R] = {
    new OptionRight(Some(right))
  }
}

final case class OptionLeft[+L, +R]
  (l: L) extends OptionEither[L, R]
