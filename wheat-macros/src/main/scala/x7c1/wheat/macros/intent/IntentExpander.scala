package x7c1.wheat.macros.intent

import android.content.Intent
import android.util.Log
import x7c1.wheat.macros.base.{IntentDecoder, TreeContext}

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

trait IntentExpanderTreeFactory extends TreeContext {
  import context.universe._
  val intentTree: Tree

  class MethodParameter(
    methodFullName: String,
    paramSymbol: Symbol,
    intent: TermName ){

    lazy val toArg: (TermName, Tree) = {
      val tree = IntentDecoder(context)(intent).
        decodeParameter(paramSymbol, methodFullName)

      val name = TermName(context freshName "x")
      name -> q"""val $name = $tree"""
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

  def createMethods(intent: TermName): Iterable[Method] = {
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
          methodFullName = method.fullName,
          paramSymbol = param,
          intent = intent
        )
      }
      Method(method.name.encodedName.toString, method.fullName, params)
    }
  }
  def findCallee = {
    val Seq(intent, action) = createTermNames("intent", "action")
    val methods = createMethods(intent)
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