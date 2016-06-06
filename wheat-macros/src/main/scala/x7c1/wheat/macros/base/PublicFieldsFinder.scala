package x7c1.wheat.macros.base

trait PublicFieldsFinder extends TreeContext {
  import context.universe._

  def findConstructorOf(targetType: Type): Option[MethodSymbol] = {
    targetType.members.
      filter(_.isConstructor).map(_.asMethod).
      find(_.paramLists exists (_.nonEmpty))
  }
  def findConstructorsOf(targetType: Type): Seq[MethodSymbol] = {
    targetType.members.
      filter(_.isConstructor).map(_.asMethod).
      filter(_.paramLists exists (_.nonEmpty)).toSeq
  }
  def methodsOf(targetType: Type): Iterable[MethodSymbol] = {
    targetType.members collect {
      case x if x.isMethod && x.isPublic => x.asMethod
    } filter {
      method =>
        ! method.isConstructor &&
        ! isBuiltInSymbol(method)
    }
  }
}
