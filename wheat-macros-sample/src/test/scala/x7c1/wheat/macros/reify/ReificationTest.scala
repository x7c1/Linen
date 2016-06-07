package x7c1.wheat.macros.reify

import org.scalatest.{FlatSpecLike, Matchers}

class ReificationTest extends FlatSpecLike with Matchers {

  it can "reify" in {
    implicitly[HasConstructor[() => HelloDecorator0]].
      newInstance().decorate("foobar") shouldBe "hello, foobar"

    Caller.execute[HelloDecorator0]() shouldBe "hello, world!"
    Caller.execute0[HelloDecorator0]() shouldBe "hello, world:0!"
    Caller.execute1[HelloDecorator1]() shouldBe "hello1, world:1! : foo"
    Caller.execute2[HelloDecorator2]() shouldBe "hello2, world:2! : foo, 345"
  }
  it can "new" in {
    val param1 = Param1(
      foo1 = "foo",
      bar1 = 123
    )
    val param2 = Param2(
      foo2 = 345,
      bar2 = 456L
    )
    val param3 = SubParam2(
      baz3 = 333,
      foo2 = 222,
      bar2 = 111
    )
    New[HelloDecorator0]().
      decorate("world") shouldBe "hello, world"

    New[HelloDecorator1](param1).
      decorate("world") shouldBe "hello1, world : foo"

    New[HelloDecorator2](param1, param2).
      decorate("world") shouldBe "hello2, world : foo, 345"

    New[HelloDecorator2](param1, param3).
      decorate("world") shouldBe "hello2, world : foo, 222"
  }
}
