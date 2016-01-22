package x7c1.wheat.macros.database

import android.content.ContentValues

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

private object TypedContentValues {
  def extract[A: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val cursor = weakTypeOf[A]
    val methods = cursor.members filter { symbol =>
      !symbol.fullName.startsWith("java.") &&
        !symbol.fullName.startsWith("scala.") &&
        !symbol.isConstructor && symbol.isMethod
    } map (_.asMethod) filter (_.paramLists.isEmpty)

    val overrides = methods map { method =>
      val key = method.name.toString
      q"override def ${TermName(key)} = ???"
    }
    val tree = q"""
      new $cursor {
        ..$overrides
        override def moveTo(n: Int) = ???
      }"""

//    println(tree)
    tree
  }

  def unwrap[A: c.WeakTypeTag](c: blackbox.Context)(pairs: c.Tree*): c.Tree = {
    import c.universe._

    def extractArgs: Tree => Option[(Tree, Tree)] = {
      case q"$x($arg)" => (x, arg) match {
        case (x: Tree, right: Tree) => extractLeft(x) map (_ -> right)
        case _ => None
      }
      case t => t.children.collectFirst{ case x => extractArgs(x) }.flatten
    }
    def extractLeft: Tree => Option[Tree] = {
      case q"$x($arg)" => (x, arg) match {
        case (_, left: Tree) => Some(left)
        case _ => None
      }
      case t =>  t.children.collectFirst { case x => extractLeft(x) }.flatten
    }
    def getColumn: Tree => String = {
      case Select(_, name) => name.toString
      case t => throw new IllegalArgumentException(s"invalid form of tree: $t")
    }
    def getJavaType: Type => Option[Type] = {
      case x if x =:= typeOf[Int] => Some(typeOf[java.lang.Integer])
      case x if x =:= typeOf[Long] => Some(typeOf[java.lang.Long])
      case x if x =:= typeOf[String] => None
      case x => throw new IllegalArgumentException(s"unsupported type: $x")
    }
    def forPrimitive(values: Tree)(left: Tree, right: Tree) = {
      val value = getJavaType(left.tpe) match {
        case Some(tpe) => q"$right: $tpe"
        case None => q"$right"
      }
      Seq(q"$values.put(${getColumn(left)}, $value)")
    }
    def forConvertible(values: Tree)(left: Tree, right: Tree) = left.tpe.typeArgs match {
      case Seq(from, to) if to =:= right.tpe =>
        val convertible = appliedType(
          typeOf[ColumnConvertible[_, _]].typeConstructor,
          from,
          to
        )
        val x = TermName(c freshName "x")
        val value = getJavaType(from) match {
          case Some(tpe) => q"$x: $tpe"
          case None => q"$x"
        }
        Seq(
          q"val $x = implicitly[$convertible].unwrap($right)",
          q"$values.put(${getColumn(left)}, $value)"
        )
      case Seq(from, to) =>
        throw new IllegalArgumentException(
          s"type inconsistent: ${getColumn(left)}:[$to] != $right:[${right.tpe}]")
      case typeArgs =>
        throw new IllegalArgumentException(s"invalid typeArgs: $typeArgs")
    }
    def toSetters(values: Tree): ((Tree, Tree)) => Seq[Tree] = {
      case (left, right) if left.tpe.widen =:= right.tpe.widen =>
        forPrimitive(values)(left, right)
      case (left, right) if left.tpe <:< typeOf[ColumnTransform[_, _]] =>
        forConvertible(values)(left, right)
      case (left, right) =>
        throw new IllegalArgumentException(
          s"type inconsistent: ${getColumn(left)}:[${left.tpe}] != $right:[${right.tpe}]")
    }
    val values = q"${TermName(c.freshName("values"))}"
    val setters = pairs map extractArgs flatMap {
      case Some(pair) => toSetters(values)(pair)
      case None => throw new IllegalArgumentException("invalid form of expression")
    }
    val tree = q"""
      val $values = new ${typeOf[ContentValues]}()
      ..$setters
      $values
    """
//    println(tree)
    tree
  }
}
