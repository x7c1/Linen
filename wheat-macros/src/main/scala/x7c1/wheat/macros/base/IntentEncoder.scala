package x7c1.wheat.macros.base

import scala.reflect.macros.blackbox

trait IntentEncoder
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val intent: TermName

  def encodeInstance() = {

  }

  def encodeParameter() = {

  }

  def isTarget(x: Type) =
    (x <:< typeOf[Boolean]) ||
    (x <:< typeOf[Long]) ||
    (x <:< typeOf[Serializable])

  def toPut(instanceType: Type, arg: TermName, prefix: String = "")(param: Symbol) = {
    val name = param.name.encodedName.toString
    val key = if (prefix.isEmpty) name else s"$prefix:$name"

    val tree = param.typeSignatureIn(instanceType) match {
      case x if isTarget(x) =>
        q"""$intent.putExtra($key, $arg.${TermName(name)})"""
      case x =>
        val paramType = x.typeSymbol.name.toString
        throw new IllegalArgumentException(
          s"unsupported type: $name: $paramType")
    }
    tree
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
