package x7c1.wheat.macros.base

import x7c1.wheat.macros.intent.ExtraNotFoundException

import scala.reflect.macros.blackbox

trait IntentDecoder
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val instanceType: Type

  def decodeIntent(intent: TermName, prefix: String = ""): Tree = {
    val pairs = findConstructorOf(instanceType).
      map(_.paramLists flatMap {_ map toGet(intent, prefix)}).
      getOrElse(List()).
      map { TermName(context freshName "x") -> _ }

    val tmps = pairs map { case (x, get) => q"val $x = $get" }
    val args = pairs map { _._1 }
    q"""
      ..$tmps
      new $instanceType(..$args)
    """
  }
  private def toGet(intent: TermName, prefix: String = "")(param: Symbol): Tree = {
    val key =
      if (prefix.isEmpty) param.fullName
      else s"$prefix:${param.fullName}"

    val tree = param.typeSignatureIn(instanceType) match {
      case x if x =:= typeOf[Long] =>
        q"$intent.getLongExtra($key, -1)"
      case x if x =:= typeOf[Boolean] =>
        q"$intent.getBooleanExtra($key, false)"
      case x if x <:< typeOf[Serializable] =>
        q"$intent.getSerializableExtra($key).asInstanceOf[$x]"
      case x =>
        throw new IllegalArgumentException(s"unsupported type : $x")
    }
    q"""
      if ($intent.hasExtra($key)){
        $tree
      } else {
        throw new ${typeOf[ExtraNotFoundException]}($key)
      }
    """
  }
}

object IntentDecoder {
  def apply
    (context0: blackbox.Context)
    (instanceType0: context0.universe.Type): IntentDecoder { val context: context0.type } =
  {
    new IntentDecoder {
      override val instanceType = instanceType0
      override val context: context0.type = context0
    }
  }
}

trait IntentTypeDecoder extends TreeContext {
  import context.universe._
  val intent: TermName

  def decodeIntent(instanceType: Type, prefix: String = ""): Tree = {
    IntentDecoder(context)(instanceType) decodeIntent (intent, prefix)
  }
}

object IntentTypeDecoder {
  def apply
    (context0: blackbox.Context)
    (intent0: context0.universe.TermName): IntentTypeDecoder { val context: context0.type } =
  {
    new IntentTypeDecoder {
      override val intent = intent0
      override val context: context0.type = context0
    }
  }
}
