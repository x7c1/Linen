package x7c1.wheat.macros.base

import x7c1.wheat.macros.intent.{PublicFieldsFinder, ExtraNotFoundException}

trait IntentDecoder
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val instanceType: Type

  def decodeIntent(intent: TermName): Tree = {
    val pairs = findConstructorOf(instanceType).
      map(_.paramLists flatMap {_ map toGet(intent)}).
      getOrElse(List()).
      map { TermName(context freshName "x") -> _ }

    val tmps = pairs map { case (x, get) => q"val $x = $get" }
    val args = pairs map { _._1 }
    q"""
      ..$tmps
      new $instanceType(..$args)
    """
  }
  def toGet(intent: TermName)(param: Symbol): Tree = {
    val key = param.fullName
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
