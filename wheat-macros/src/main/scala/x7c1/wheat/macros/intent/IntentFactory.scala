package x7c1.wheat.macros.intent

import android.content.{Intent, Context}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object IntentFactory {
  def using[A]: IntentFactory[A] = new IntentFactory[A]
}

class IntentFactory[A]{
  def create(context: Context, klass: Class[_])(f: A => Unit): Intent =
    macro IntentFactoryImpl.createIntent[A]
}

private object IntentFactoryImpl {
  def createIntent[A: c.WeakTypeTag](c: blackbox.Context)
      (context: c.Tree, klass: c.Tree)(f: c.Tree): c.Tree = {

    import c.universe._
    val factory = new ServiceCallerTreeFactory {
      override val context: c.type = c
      override val block = f
    }
    val intent = factory.intent
    val tree = q"""
      val $intent = new ${typeOf[Intent]}($context, $klass)
      $intent.setAction(${factory.methodName})
      ..${factory putExtras intent}
      $intent
       """

    println(showCode(tree))
    tree
  }
}
