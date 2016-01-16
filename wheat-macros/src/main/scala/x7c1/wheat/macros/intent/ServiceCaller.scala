package x7c1.wheat.macros.intent

import android.content.{Intent, Context}
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

    case class IntentExtra(
      key: String,
      value: Tree,
      typeName: String
    )
    val (_, call) = f.children match {
      case Seq(x, y: Apply) => x -> y
      case _ => throw new IllegalArgumentException("invalid form of expression")
    }
    val pairs = call.symbol.asMethod.paramLists match {
      case xs if xs.length > 1 =>
        throw new IllegalArgumentException(s"too many paramLists : $xs")
      case Seq(args) =>
        args map { arg =>
          arg.name.encodedName.toString ->
            arg.typeSignature.typeSymbol.fullName
        }
    }
    val methodName = call.symbol.asMethod.fullName
    val extras = pairs zip call.children.tail map {
      case ((argName, argType), arg) =>
        IntentExtra(
          key = s"$methodName.$argName",
          value = arg,
          typeName = argType )
    }
    val intent = TermName(c freshName "intent")
    val putExtras = extras map { extra =>
      q"$intent.putExtra(${extra.key}, ${extra.value})"
    }
    val tree = q"""
      val $intent = new ${typeOf[Intent]}($context, $klass)
      $intent.setAction($methodName)
      ..$putExtras
      $context.startService($intent)
    """

//    println(showCode(tree))
    tree
  }
}

trait BundleConvertible[A] {
  def toBundle(target: A): Bundle
}
