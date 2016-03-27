package x7c1.wheat.macros.intent

import android.content.{Context, Intent}
import android.support.v4.content.LocalBroadcastManager
import x7c1.wheat.macros.base.PublicFieldsFinder

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


object LocalBroadcaster {
  def apply[A](event: A): LocalBroadcaster = macro LocalBroadcasterImpl.create
}

class LocalBroadcaster(toIntent: () => Intent){
  def dispatchFrom(context: Context): Unit = {
    val intent = toIntent()
    LocalBroadcastManager.getInstance(context) sendBroadcast intent
  }
}

private object LocalBroadcasterImpl {
  def create(c: blackbox.Context)(event: c.Tree): c.Tree = {
    import c.universe._
    val factory = new ActionIntentTreeFactory {
      override val context: c.type = c
      override val eventTree = event
    }
    val toIntent = factory.toIntent
    val tree =
      q"""
        val toIntent = $toIntent
        new ${typeOf[LocalBroadcaster]}(toIntent)
      """

//    println(tree)
    tree
  }
}

private trait ActionIntentTreeFactory extends PublicFieldsFinder {
  import context.universe._
  val eventTree: Tree

  private lazy val eventName = eventTree.tpe.typeSymbol.name.toString
  private lazy val eventFullName = eventTree.tpe.typeSymbol.fullName

  def toIntent: Tree = {
    val Seq(intent, event) = createTermNames("intent", "event")
    val putExtras = findConstructorOf(eventTree.tpe).
      map(_.paramLists flatMap {_ map toPut(intent, event)}) getOrElse List()

    val action = eventTree.tpe.typeSymbol.fullName
    q"""
      () => {
        val $event = $eventTree
        val $intent = new ${typeOf[Intent]}($action)
        ..$putExtras
        $intent
      }
    """
  }
  def isTarget(x: Type) =
    (x <:< typeOf[Boolean]) ||
    (x <:< typeOf[Long]) ||
    (x <:< typeOf[Serializable])

  def toPut(intent: TermName, arg: TermName)(param: Symbol) = {
    val name = param.name.toString
    val tree = param.typeSignatureIn(eventTree.tpe) match {
      case x if isTarget(x) =>
        val key = s"$eventFullName:$name"
        q"""$intent.putExtra($key, $arg.${TermName(name)})"""
      case x =>
        val paramType = x.typeSymbol.name.toString
        throw new IllegalArgumentException(
          s"unsupported type: $eventName#$name: $paramType")
    }
    tree
  }
}

