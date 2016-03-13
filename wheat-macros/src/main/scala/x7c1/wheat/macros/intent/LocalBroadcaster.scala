package x7c1.wheat.macros.intent

import android.content.{Context, Intent}
import android.support.v4.content.LocalBroadcastManager

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object LocalBroadcaster {
  def of[A](event: A): LocalBroadcaster = macro LocalBroadcasterImpl.create
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

    println(tree)
    tree
  }
}

private trait ActionIntentTreeFactory {
  val context: blackbox.Context
  import context.universe._
  val eventTree: Tree

  def toIntent: Tree = {
    println(eventTree.tpe)
    q"""
      () => {
        val intent = new ${typeOf[Intent]}()
        intent
      }
    """
  }
}
