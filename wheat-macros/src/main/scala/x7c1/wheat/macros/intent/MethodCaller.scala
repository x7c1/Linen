package x7c1.wheat.macros.intent

import android.content.Intent
import android.util.Log

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object MethodCaller {
  def findFrom(intent: Intent): Either[MethodCallerError, () => Unit] =
    macro MethodCallerImpl.findMethod[Intent]

  def executeBy(intent: Intent): Unit =
    macro MethodCallerImpl.executeMethod[Intent]
}

private object MethodCallerImpl {
  def findMethod[A: c.WeakTypeTag](c: blackbox.Context)(intent: c.Tree): c.Tree = {

    val factory = new MethodCallerTreeFactory {
      override val context: c.type = c
      override val intentTree = intent
    }
    val tree = factory.findCallee(intent)
//    println(showCode(tree))
    tree
  }
  def executeMethod[A: c.WeakTypeTag](c: blackbox.Context)(intent: c.Tree): c.Tree = {
    import c.universe._

    val factory = new MethodCallerTreeFactory {
      override val context: c.type = c
      override val intentTree = intent
    }
    val f = TermName(c freshName "f")
    val callee = TermName(c freshName "callee")

    val tree = q"""
      val $callee = ${factory findCallee intent}
      $callee match {
        case Right($f) =>
          try $f()
          catch { case ${factory.caseIllegalArgument} }
        case ${factory.caseUnknownAction}
      }
    """

//    println(showCode(tree))
    tree
  }
}

trait MethodCallerTreeFactory {
  val context: blackbox.Context
  import context.universe._
  val intentTree: Tree

  class MethodParameter(
    key: String,
    paramName: String,
    paramType: Type ){

    lazy val toArg: (TermName, Tree) = {
      val tree = paramType match {
        case x if x =:= typeOf[Int] => q"$intentTree.getIntExtra($key, -1)"
        case x if x =:= typeOf[Long] => q"$intentTree.getLongExtra($key, -1)"
        case x if x =:= typeOf[String] => q"$intentTree.getStringExtra($key)"
        case x =>
          throw new IllegalArgumentException(s"unsupported type : $x")
      }
      val name = TermName(context freshName "x")
      val message = s"extra not assigned: $key"
      name -> q"""
        val $name =
          if ($intentTree.hasExtra($key)) $tree
          else throw new IllegalArgumentException($message)"""
    }
  }
  case class Method(
    name: String,
    fullName: String,
    params: Seq[MethodParameter] ){

    def toCase = {
      val method = TermName(name)
      val pairs = params.map(_.toArg)
      val args = pairs.map(_._1)
      val assigns = pairs.map(_._2)
      cq"""
        $fullName => Right(() => {
          ..$assigns
          $method(..$args)
        })
        """
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
      case x if x.isMethod && x.isPublic =>
        x.asMethod
    } filter {
      method => ! method.isConstructor &&
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
  def findCallee(intent: Tree) = {
    val methods = createMethods
    val unknown = typeOf[UnknownAction]
    q"""
      $intent.getAction match {
        case ..${methods.map(_.toCase)}
        case action => Left apply new $unknown(action)
      }
      """
  }
  def caseUnknownAction = {
    val Log = typeOf[Log].companion
    val tag = enclosingClass.fullName
    val x = TermName(context freshName "x")
    cq""" $x =>
      $Log.e($tag, "unknown action: " + $x)
      """
  }
  def caseIllegalArgument = {
    val Log = typeOf[Log].companion
    val tag = enclosingClass.fullName
    val x = TermName(context freshName "x")
    cq""" $x: IllegalArgumentException =>
      $Log.e($tag, $x.getMessage)
      """
  }
}

sealed trait MethodCallerError

case class UnknownAction(action: String) extends MethodCallerError