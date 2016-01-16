package x7c1.wheat.macros.intent

import android.content.{Context, Intent}
import android.os.Bundle
import android.util.Log

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
      case Seq(params) =>
        params map { param =>
          param.name.encodedName.toString ->
            param.typeSignature.typeSymbol.fullName
        }
    }
    val methodName = call.symbol.asMethod.fullName
    val extras = pairs zip call.children.tail map {
      case ((paramName, paramType), arg) =>
        IntentExtra(
          key = s"$methodName.$paramName",
          value = arg,
          typeName = paramType )
    }
    val intent = TermName(c freshName "intentss")
    val putExtras = extras map { extra =>
      q"$intent.putExtra(${extra.key}, ${extra.value})"
    }
    val tree = q"""{
      val $intent = new ${typeOf[Intent]}($context, $klass)
      $intent.setAction($methodName)
      ..$putExtras
      $context.startService($intent)
    }"""

    println(showCode(tree))
    tree
  }
}

object MethodCaller {
  def via(intent: Intent): Unit =
    macro MethodCallerImpl.callMethod[Intent]
}

private object MethodCallerImpl {
  def callMethod[A: c.WeakTypeTag](c: blackbox.Context)(intent: c.Tree): c.Tree = {
    import c.universe._
    println(intent)
    println(intent.symbol)
    println(intent.symbol.owner)
    val klass = intent.symbol.owner.owner.asClass

    val methodSymbols = klass.typeSignature.members collect {
      case x if x.isMethod && x.isPublic => x.asMethod
    } filter {
      method =>
        ! method.isConstructor &&
        ! method.fullName.startsWith("java.lang.") &&
        ! method.fullName.startsWith("scala.")
    } filter {
      method =>
        method.paramLists.length == 1
    }
    println("------")
    methodSymbols.foreach(println)

    case class MethodParameter(
      key: String,
      paramName: String,
      paramType: Type ){

      def toArg: Tree = {
        val tree = paramType match {
          case x if x =:= typeOf[Int] => q"$intent.getIntExtra($key, -1)"
          case x if x =:= typeOf[Long] => q"$intent.getLongExtra($key, -1)"
          case x if x =:= typeOf[String] => q"$intent.getStringExtra($key)"
          case _ => q""
        }
        tree
      }
    }
    case class Method(
      name: String,
      fullName: String,
      params: Seq[MethodParameter] ){

      def toCase = {
        val key = Literal(Constant(fullName))
        val method = TermName(name)
        val args = params.map(_.toArg)
        cq"$key => $method(..$args)"
      }
    }
    val methods = methodSymbols map { method =>
      val params = method.paramLists.head map { param =>
        val paramName = param.name.encodedName.toString
        MethodParameter(
          key = s"${method.fullName}.$paramName",
          paramName = paramName,
          paramType = param.typeSignature
        )
      }
      Method(method.name.encodedName.toString, method.fullName, params)
    }
    methods foreach println

    val Log = typeOf[Log].companion
    val tag = Literal(Constant(klass.fullName))
    val tree =
      q"""
        $intent.getAction match {
          case ..${methods.map(_.toCase)}
          case x => $Log.e($tag, "unknown action : " + x)
        }
       """

    println(showCode(tree))
    tree
  }
}

trait BundleConvertible[A] {
  def toBundle(target: A): Bundle
}
