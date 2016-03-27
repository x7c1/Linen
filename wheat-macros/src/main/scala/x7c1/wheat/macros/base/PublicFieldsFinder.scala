package x7c1.wheat.macros.base

trait PublicFieldsFinder extends TreeContext {
  import context.universe._

  def findConstructorOf(targetType: Type): Option[MethodSymbol] = {
    targetType.members.
      filter(_.isConstructor).map(_.asMethod).
      find(_.paramLists exists (_.nonEmpty))
  }
}
