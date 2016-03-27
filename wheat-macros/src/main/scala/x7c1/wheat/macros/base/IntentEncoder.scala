package x7c1.wheat.macros.base

import scala.reflect.macros.blackbox

trait IntentEncoder
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val intent: TermName

  def encodeInstance(
    instanceType: Type, instance: Either[TermName, Tree], prefix: String = "") = {

    val toPut0 = toPut(instanceType, instance, prefix) _
    findConstructorOf(instanceType).
      map(_.paramLists flatMap {_ map toPut0}) getOrElse List()
  }

  def isTarget(x: Type) =
    (x <:< typeOf[Boolean]) ||
    (x <:< typeOf[Long]) ||
    (x <:< typeOf[Serializable])

  def toPut
    (instanceType: Type, instance: Either[TermName, Tree], prefix: String = "")
    (param: Symbol): Tree = {

    val name = param.name.encodedName.toString
    val key = if (prefix.isEmpty) name else s"$prefix:$name"
    val extra = TermName(context freshName "extra")
    param.typeSignatureIn(instanceType) match {
      case x if isTarget(x) =>
        val assign = instance match {
          case Right(tree) => q"""$tree.${TermName(name)}"""
          case Left(arg) => q"""$arg.${TermName(name)}"""
        }
        q"""
          val $extra = $assign
          $intent.putExtra($key, $extra)
        """
      case x if ! isBuiltInType(x) =>
        val select = instance match {
          case Right(tree) => q"$tree.${TermName(name)}"
          case Left(arg) => q"$arg.${TermName(name)}"
        }
        val trees = encodeInstance(x, Right(select), s"$prefix:$name")
        q"""..$trees"""
      case x =>
        val paramType = x.typeSymbol.name.toString
        throw new IllegalArgumentException(
          s"unsupported type: $name: $paramType")
    }
  }
}

object IntentEncoder {
  def apply
    (context0: blackbox.Context)
    (intent0: context0.universe.TermName): IntentEncoder { val context: context0.type } = {

    new IntentEncoder {
      override val intent = intent0
      override val context: context0.type = context0
    }
  }
}
