package x7c1.wheat.macros.intent

import android.content.Context
import android.support.v4.content.LocalBroadcastManager

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object LocalBroadcaster {
  def dispatch[A](context: Context, event: A): Unit = macro LocalBroadcasterImpl.dispatch[A]
}

private object LocalBroadcasterImpl {
  def dispatch[A: c.WeakTypeTag]
    (c: blackbox.Context)
    (context: c.Tree, event: c.Tree): c.Tree = {

    val factory = new ActionIntentTreeFactory {
      override val context: c.type = c
      override val eventTree = event
    }
    val tree = factory.dispatch(context)
    println(c.universe.showCode(tree))
    tree
  }
}

private trait ActionIntentTreeFactory {
  val context: blackbox.Context
  import context.universe._
  val eventTree: Tree

  def dispatch(androidContext: Tree):  Tree = {
    val intent = TermName(context freshName "intent")
    val manager = TermName(context freshName "manager")
    q"""
      val $intent = new Intent()
      val $manager = ${typeOf[LocalBroadcastManager].companion}.getInstance($androidContext)
      $manager.sendBroadcast($intent)
     """
  }
}