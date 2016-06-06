package x7c1.wheat.macros.base

trait MethodComparator extends TreeContext {
  import context.universe._

  def hasSameParameterTypes(method: MethodSymbol, function: Type): Boolean = {
    println(method)

    ???
  }
  def methodToFunction(method: MethodSymbol): Tree = {
    ???
  }
}
