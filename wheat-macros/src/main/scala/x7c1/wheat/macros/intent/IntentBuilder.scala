package x7c1.wheat.macros.intent

import android.content.{Context, Intent}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


trait IntentBuilder[A]{
  def build(context: Context, klass: Class[_]): Intent
}

object IntentBuilder {
  def from[A](f: A => Unit): IntentBuilder[A] =
    macro IntentBuilderImpl.buildIntent[A]
}

private object IntentBuilderImpl {
  def buildIntent[A: c.WeakTypeTag](c: blackbox.Context)(f: c.Tree): c.Tree = {
    val factory = new IntentTreeFactory {
      override val context: c.type = c
      override val block = f
    }
    val tree = factory.newBuilder[A]()
//    println(tree)
    tree
  }
}
