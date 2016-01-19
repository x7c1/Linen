package x7c1.wheat.macros.intent

import android.content.Intent
import android.util.Log

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object IntentExpander {
  def findFrom(intent: Intent): Either[IntentNotExpanded, () => Unit] =
    macro IntentExpanderImpl.findMethod

  def executeBy(intent: Intent): Unit =
    macro IntentExpanderImpl.executeMethod
}

private object IntentExpanderImpl {
  def findMethod(c: blackbox.Context)(intent: c.Tree): c.Tree = {
    val factory = new IntentExpanderTreeFactory {
      override val context: c.type = c
      override val intentTree = intent
    }
    val tree = factory.findCallee
//    println(c.universe.showCode(tree))
    tree
  }
  def executeMethod(c: blackbox.Context)(intent: c.Tree): c.Tree = {
    val factory = new IntentExpanderTreeFactory {
      override val context: c.type = c
      override val intentTree = intent
    }
    val tree = factory.executeCallee
//    println(c.universe.showCode(tree))
    tree
  }
}

trait IntentExpanderTreeFactory {
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
      val exception = typeOf[ExtraNotFoundException]
      name -> q"""
        val $name =
          if ($intentTree.hasExtra($key)) $tree
          else throw new $exception($key)"""
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
        $fullName => try {
          ..$assigns
          Right(() => $method(..$args))
        } catch {
          case e: ${typeOf[ExtraNotFoundException]} =>
            Left(new ${typeOf[ExtraNotFound]}(e.key))
        }
        """
    }
  }

  lazy val enclosingClass = {
    @tailrec
    def traverse(x: Symbol): ClassSymbol = {
      if (x.isClass) x.asClass
      else traverse(x.owner)
    }
    traverse(context.internal.enclosingOwner)
  }

  lazy val enclosingMethod = {
    @tailrec
    def traverse(x: Symbol): MethodSymbol = {
      if (x.isMethod) x.asMethod
      else traverse(x.owner)
    }
    traverse(context.internal.enclosingOwner)
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
  def createTermNames(names: String*): Seq[TermName] = {
    names map (x => TermName(context freshName x))
  }
  def findCallee = {
    val methods = createMethods
    val Seq(intent, action) = createTermNames("intent", "action")
    q"""
      Option($intentTree) match {
        case Some($intent) => Option($intent.getAction) match {
          case Some($action) => $action match {
            case ..${methods.map(_.toCase)}
            case _ =>
              Left apply new ${typeOf[UnknownAction]}($action)
          }
          case None =>
            Left apply new ${typeOf[NoIntentAction]}
        }
        case _ => Left apply new ${typeOf[NoIntent]}
      }
      """
  }
  def executeCallee = {
    val Seq(f, e, callee) = createTermNames("f", "e", "callee")
    val log = typeOf[Log].companion
    val tag = enclosingMethod.fullName
    q"""
      val $callee = $findCallee
      $callee match {
        case Right($f) => $f()
        case Left($e) => $log.e($tag, $e.toString)
      }
    """
  }
}

class ExtraNotFoundException(
  val key: String) extends Exception