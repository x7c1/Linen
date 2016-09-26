package x7c1.wheat.macros.base

import x7c1.wheat.macros.intent.ExtraNotFoundException

import scala.reflect.macros.blackbox

trait IntentDecoder
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val intent: TermName

  def decodeIntent(instanceType: Type, prefix: String = ""): Tree = {
    val pairs = findConstructorOf(instanceType).
      map(_.paramLists flatMap {_ map toGet(instanceType, prefix)}).
      getOrElse(List()).
      map { TermName(context freshName "x") -> _ }

    val tmps = pairs map { case (x, get) => q"val $x = $get" }
    val args = pairs map { _._1 }
    q"""
      ..$tmps
      new $instanceType(..$args)
    """
  }
  def decodeParameter(param: Symbol, prefix: String = ""): Tree = {
    toGet(param.typeSignature, prefix)(param)
  }

  private def toGet(instanceType: Type, prefix: String = "")(param: Symbol): Tree = {
    val name = param.name.encodedName.toString
    val key = if (prefix.isEmpty) name else s"$prefix:$name"
    val tree = param.typeSignatureIn(instanceType) match {
      case x if x =:= typeOf[Int] =>
        q"$intent.getIntExtra($key, -1)"
      case x if x =:= typeOf[Long] =>
        q"$intent.getLongExtra($key, -1)"
      case x if x =:= typeOf[String] =>
        q"$intent.getStringExtra($key)"
      case x if x =:= typeOf[Boolean] =>
        q"$intent.getBooleanExtra($key, false)"
      case x if x =:= typeOf[Seq[Long]] =>
        q"$intent.getLongArrayExtra($key).toSeq"
      case x if x <:< typeOf[Serializable] =>
        q"$intent.getSerializableExtra($key).asInstanceOf[$x]"
      case x if ! isBuiltInType(x) =>
        decodeIntent(x, prefix = s"$prefix:$name")
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
    (intent0: context0.universe.TermName): IntentDecoder { val context: context0.type } =
  {
    new IntentDecoder {
      override val intent = intent0
      override val context: context0.type = context0
    }
  }
}
