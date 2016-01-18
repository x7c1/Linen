package x7c1.wheat.macros.intent

import android.content.Context
import android.os.Bundle

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ServiceCaller {
  def using[A]: ServiceCaller[A] = new ServiceCaller[A]
}

class ServiceCaller[A]{
  def startService(context: Context, klass: Class[_])(f: A => Unit): Unit =
    macro ServiceCallerImpl.startService[A]
}

private object ServiceCallerImpl {
  def startService[A: c.WeakTypeTag](c: blackbox.Context)
    (context: c.Tree, klass: c.Tree)(f: c.Tree): c.Tree = {
    import c.universe._

    val factory = new IntentTreeFactory {
      override val context: c.type = c
      override val block = f
    }
    val intent = TermName(c freshName "intent")
    val tree = q"""
      val $intent = ${factory.newIntent(context, klass)}
      $context.startService($intent)
    """

//    println(showCode(tree))
    tree
  }
}

trait BundleConvertible[A] {
  def toBundle(target: A): Bundle
}
