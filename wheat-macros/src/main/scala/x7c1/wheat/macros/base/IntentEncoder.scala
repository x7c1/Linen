package x7c1.wheat.macros.base

import scala.reflect.macros.blackbox

trait IntentEncoder
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val intent: TermName

  def encodeInstance(
    instanceType: Type,
    instance: Tree, prefix: String = ""): List[Tree] = {

    val toPut0 = toPut(instanceType, instance, prefix) _
    findConstructorOf(instanceType).
      map(_.paramLists flatMap {_ map toPut0}) getOrElse List()
  }

  private def isPrimitive(x: Type) =
    (x <:< typeOf[Boolean]) ||
    (x <:< typeOf[Long]) ||
    (x <:< typeOf[Int]) ||
    (x <:< typeOf[String])

  private def isSerializable(x: Type) =
    x <:< typeOf[Serializable]

  private def isSeq(x: Type) =
    x =:= typeOf[Seq[Long]]

  def toPut
    (instanceType: Type, instance: Tree, prefix: String = "")
    (param: Symbol): Tree = {

    val name = param.name.encodedName.toString
    buildIntent(
      targetType = param typeSignatureIn instanceType,
      select = q"""$instance.${TermName(name)}""",
      name = name,
      prefix = prefix
    )
  }
  def buildIntent(
    targetType: Type,
    select: Tree,
    name: String,
    prefix: String = ""): Tree = {

    val key = if (prefix.isEmpty) name else s"$prefix:$name"
    val extra = TermName(context freshName "extra")
    targetType match {
      case x if isPrimitive(x) || isSerializable(x) =>
        q"""
          val $extra = $select
          $intent.putExtra($key, $extra)
        """
      case x if isSeq(x) =>
        q"""
          val $extra = $select
          $intent.putExtra($key, $extra.toArray)
        """
      case x if ! isBuiltInType(x) =>
        val trees = encodeInstance(x, select, s"$prefix:$name")
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
