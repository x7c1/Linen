package x7c1.wheat.macros.reify

class HelloDecorator0 extends SampleDecorator {
  override def decorate(arg: String) = {
    s"hello, $arg"
  }
}
class HelloDecorator1(p1: Param1) extends SampleDecorator {
  override def decorate(arg: String) = {
    s"hello1, $arg : ${p1.foo1}"
  }
}

class HelloDecorator2(p1: Param1, p2: Param2) extends SampleDecorator {
  override def decorate(arg: String) = {
    s"hello2, $arg : ${p1.foo1}, ${p2.foo2}"
  }
}
