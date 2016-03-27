package x7c1.wheat.macros.intent

import android.content.{Context, Intent}
import android.support.v4.content.LocalBroadcastManager
import x7c1.wheat.macros.base.{IntentEncoder, PublicFieldsFinder}

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
    val encoder = IntentEncoder(context)(intent)
    val toPut = encoder.toPut(eventTree.tpe, event, prefix = eventFullName) _

    val putExtras = findConstructorOf(eventTree.tpe).
      map(_.paramLists flatMap {_ map toPut}) getOrElse List()

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

}

