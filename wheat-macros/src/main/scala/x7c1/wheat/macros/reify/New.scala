package x7c1.wheat.macros.reify

object New {
  def apply[A]: Applied[A] = new Applied[A]

  class Applied[A]{
    def apply()(implicit i: HasConstructor[() => A]): A = {
      i.newInstance()
    }
    def apply[X1](x1: X1)(implicit i: HasConstructor[X1 => A]): A = {
      i.newInstance(x1)
    }
    def apply[X1, X2](x1: X1, x2: X2)(implicit i: HasConstructor[(X1, X2) => A]): A = {
      i.newInstance(x1, x2)
    }
    def apply[X1, X2, X3](x1: X1, x2: X2, x3: X3)(implicit i: HasConstructor[(X1, X2, X3) => A]): A = {
      i.newInstance(x1, x2, x3)
    }
  }
}
