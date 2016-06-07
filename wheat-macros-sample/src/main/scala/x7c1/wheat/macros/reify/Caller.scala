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
case class Param2(
  foo2: Int,
  bar2: Long
)
