package x7c1.wheat.macros.reify

import org.scalatest.{FlatSpecLike, Matchers}

class ReificationTest extends FlatSpecLike with Matchers {

  it can "reify" in {
    implicitly[HasConstructor[() => HelloDecorator0]].
      newInstance().decorate("foobar") shouldBe "hello, foobar"

    Caller.execute[HelloDecorator0]() shouldBe "hello, world!"
    Caller.execute0[HelloDecorator0]() shouldBe "hello, world:0!"
    Caller.execute1[HelloDecorator1]() shouldBe "hello1, world:1! : Param1(foo,123)"
    Caller.execute2[HelloDecorator2]() shouldBe "hello2, world:2! : Param1(foo,123), Param2(345,456)"
  }
}
