package x7c1.wheat.macros.logger

import x7c1.wheat.macros.base.TreeContext

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Log {
  def verbose(message: => String): Unit = macro LogImpl.verbose[String]
  def debug  (message: => String): Unit = macro LogImpl.debug[String]
  def info   (message: => String): Unit = macro LogImpl.info[String]
  def warn   (message: => String): Unit = macro LogImpl.warn[String]
  def error  (message: => String): Unit = macro LogImpl.error[String]
}

private object LogImpl {
  import scala.language.existentials

  def verbose[A: c.WeakTypeTag](c: blackbox.Context)(message: c.Expr[A]): c.Tree = {
    new TreeFactory[c.type](c).create(message, "v")
  }
  def debug[A: c.WeakTypeTag](c: blackbox.Context)(message: c.Expr[A]): c.Tree = {
    new TreeFactory[c.type](c).create(message, "d")
  }
  def info[A: c.WeakTypeTag](c: blackbox.Context)(message: c.Expr[A]): c.Tree = {
    new TreeFactory[c.type](c).create(message, "i")
  }
  def warn[A: c.WeakTypeTag](c: blackbox.Context)(message: c.Expr[A]): c.Tree = {
    new TreeFactory[c.type](c).create(message, "w")
  }
  def error[A: c.WeakTypeTag](c: blackbox.Context)(message: c.Expr[A]): c.Tree = {
    new TreeFactory[c.type](c).create(message, "e")
  }
}

private class TreeFactory[C <: blackbox.Context](val context: C) extends TreeContext {
  import context.universe._

  def create[A: context.WeakTypeTag](message: context.Expr[A], method: String) = {
    val logger = weakTypeOf[android.util.Log].companion
    q"""$logger.${TermName(method)}(${enclosing.fullName}, $message)"""
  }
}
