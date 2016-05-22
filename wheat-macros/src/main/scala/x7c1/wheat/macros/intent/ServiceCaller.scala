package x7c1.wheat.macros.intent

import android.content.Context
import android.os.Bundle
import x7c1.wheat.macros.base.PublicFieldsFinder

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ServiceCaller {
  def using[A]: ServiceCaller[A] = new ServiceCaller[A]

  def reify[A](context: Context, klass: Class[_]): A =
    macro ServiceCallerImpl.reify[A]
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

  def reify[A: c.WeakTypeTag](c: blackbox.Context)(context: c.Tree, klass: c.Tree): c.Tree = {
    val androidContext = context
    val caller = new ServiceCallerTreeFactory {
      override val context: c.type = c
      override val contextTree = androidContext
      override val klassTree = klass
      override val serviceType = c.universe.weakTypeOf[A]
    }
    val tree = caller.reify
//    println(tree)
    tree
  }
}

trait ServiceCallerTreeFactory extends PublicFieldsFinder {
  import context.universe._

  /* android.content.Context */
  val contextTree: Tree

  /* java.lang.Class */
  val klassTree: Tree

  val serviceType: Type

  def reify = {
    val methods = methodsOf(serviceType) map { method =>
      val paramLists = method.paramLists map { params =>
        params map { param =>
          q"${param.name.encodedName.toTermName}: ${param.typeSignature}"
        }
      }
      val argLists = method.paramLists map { params =>
        params map { param =>
          q"${param.name.encodedName.toTermName}"
        }
      }
      q"""
        override def ${method.name.encodedName.toTermName}(...$paramLists) =
          ${typeOf[ServiceCaller[_]].companion}.using[$serviceType].
            startService($contextTree, $klassTree){
              _.${method.name.encodedName.toTermName}(...$argLists)
            }
      """
    }
    val tree = q"""new $serviceType { ..$methods }"""
//    println(tree)
    tree
  }

}

trait BundleConvertible[A] {
  def toBundle(target: A): Bundle
}
