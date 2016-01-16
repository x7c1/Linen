package x7c1.wheat.macros.intent

import android.content.{Context, Intent}
import android.os.Bundle
import android.util.Log

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.{Universe, blackbox}

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
    macro MethodCallerImpl.expandMethods[Intent]
}

private object MethodCallerImpl {
  def expandMethods[A: c.WeakTypeTag](c: blackbox.Context)(intent: c.Tree): c.Tree = {
    import c.universe._

    val factory = new MethodCallerTreeFactory {
      val universe: c.universe.type = c.universe
      val intentTree = intent
    }
    val methods = factory.createMethods
    val tree = q"""
      $intent.getAction match {
        case ..${methods.map(_.toCase)}
        case ${factory.caseUnknownAction}
      }
     """

    println(showCode(tree))
    tree
  }
}

trait MethodCallerTreeFactory {
  val universe: Universe

  import universe._
  val intentTree: Tree

  class MethodParameter(
    key: String,
    paramName: String,
    paramType: Type ){

    def toArg: Tree = {
      val tree = paramType match {
        case x if x =:= typeOf[Int] => q"$intentTree.getIntExtra($key, -1)"
        case x if x =:= typeOf[Long] => q"$intentTree.getLongExtra($key, -1)"
        case x if x =:= typeOf[String] => q"$intentTree.getStringExtra($key)"
        case x =>
          throw new IllegalArgumentException(s"unsupported type : $x")
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

  lazy val enclosingClass = {
    @tailrec
    def traverse(x: Symbol): ClassSymbol = {
      if (x.isClass) x.asClass
      else traverse(x.owner)
    }
    traverse(intentTree.symbol)
  }

  lazy val enclosingMethod = {
    @tailrec
    def traverse(x: Symbol): MethodSymbol = {
      if (x.isMethod) x.asMethod
      else traverse(x.owner)
    }
    traverse(intentTree.symbol)
  }

  def createMethods: Iterable[Method] = {
    val methodSymbols = enclosingClass.typeSignature.members collect {
      case x if x.isMethod && x.isPublic => x.asMethod
    } filter {
      method =>
        ! method.isConstructor &&
        ! method.fullName.startsWith("java.lang.") &&
        ! method.fullName.startsWith("scala.") &&
        ! (method == enclosingMethod)
    } filter {
      _.paramLists.length == 1
    }
    methodSymbols map { method =>
      val params = method.paramLists.head map { param =>
        val paramName = param.name.encodedName.toString
        new MethodParameter(
          key = s"${method.fullName}.$paramName",
          paramName = paramName,
          paramType = param.typeSignature
        )
      }
      Method(method.name.encodedName.toString, method.fullName, params)
    }
  }
  def caseUnknownAction = {
    val Log = typeOf[Log].companion
    val tag = Literal(Constant(enclosingClass.fullName))
    cq"""x => $Log.e($tag, "unknown action : " + x)"""
  }
}

trait BundleConvertible[A] {
  def toBundle(target: A): Bundle
}
