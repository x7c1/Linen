package x7c1.wheat.macros.reify

import x7c1.wheat.macros.reify.HasConstructor.HasDefaultConstructor

object Caller {
  val param1 = Param1(
    foo1 = "foo",
    bar1 = 123
  )
  val param2 = Param2(
    foo2 = 345,
    bar2 = 456L
  )
  def execute[A <: SampleDecorator: HasDefaultConstructor]() = {
    val decorator = implicitly[HasDefaultConstructor[A]].newInstance()
    decorator decorate "world!"
  }
  def execute0[A <: SampleDecorator]()(implicit i: HasConstructor[() => A]) = {
    val decorator = i.newInstance()
    decorator decorate "world:0!"
  }
  def execute1[A <: SampleDecorator]()(implicit i: HasConstructor[Param1 => A]) = {
    val decorator = i.newInstance(param1)
    decorator decorate "world:1!"
  }
  def execute2[A <: SampleDecorator]()(implicit i: HasConstructor[(Param1, Param2) => A]) = {
    val decorator = i.newInstance(param1, param2)
    decorator decorate "world:2!"
  }
}

trait SampleDecorator {
  def decorate(arg: String): String
}
case class Param1(
  foo1: String,
  bar1: Int
)
trait Param2 {
  def foo2: Int
  def bar2: Long
}
object Param2 {
  def apply(foo2: Int, bar2: Long): Param2 = {
    val (tmp1, tmp2) = (foo2, bar2)
    new Param2 {
      override val foo2: Int = tmp1
      override val bar2: Long = tmp2
    }
  }
}

case class SubParam2(
  baz3: Int,
  foo2: Int,
  bar2: Long ) extends Param2
