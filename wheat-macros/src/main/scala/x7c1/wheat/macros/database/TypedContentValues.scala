package x7c1.wheat.macros.database

import android.content.ContentValues

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

private object TypedContentValues {
  def extract[A: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val field = weakTypeOf[A]
    val methods = field.members filter { symbol =>
      !symbol.fullName.startsWith("java.") &&
        !symbol.fullName.startsWith("scala.") &&
        !symbol.isConstructor && symbol.isMethod
    } map (_.asMethod) filter (_.paramLists.isEmpty)

    val overrides = methods map { method =>
      val key = method.name.toString
      q"override def ${TermName(key)} = ???"
    }
    val tree = q"""
      new $field {
        ..$overrides
      }"""

//    println(tree)
    tree
  }

  def unwrap[A: c.WeakTypeTag](c: blackbox.Context)(pairs: c.Tree*): c.Tree = {
    import c.universe._

    val parser = new TypedFieldsParser {
      override val context: c.type  = c
    }
    def forPrimitive(values: Tree)(left: Tree, right: Tree) = {
      val value = parser.getJavaType(left.tpe) match {
        case Some(tpe) if right.tpe <:< typeOf[Option[_]] =>
          val x = TermName(c freshName "x")
          q"""$right match {
            case Some($x) => $x : $tpe
            case None => null : $tpe
          }"""
        case Some(tpe) => q"$right: $tpe"
        case None => q"$right"
      }
      Seq(q"$values.put(${parser.getColumn(left)}, $value)")
    }
    def forConvertible(values: Tree)(left: Tree, right: Tree) = left.tpe.typeArgs match {
      case Seq(from, to) if to =:= right.tpe =>
        val convertible = appliedType(
          typeOf[FieldConvertible[_, _]].typeConstructor,
          from,
          to
        )
        val x = TermName(c freshName "x")
        val value = parser.getJavaType(from) match {
          case Some(tpe) => q"$x: $tpe"
          case None => q"$x"
        }
        Seq(
          q"val $x = implicitly[$convertible].unwrap($right)",
          q"$values.put(${parser.getColumn(left)}, $value)"
        )
      case Seq(from, to) =>
        throw new IllegalArgumentException(
          s"type inconsistent: ${parser.getColumn(left)}:[$to] != $right:[${right.tpe}]")
      case typeArgs =>
        throw new IllegalArgumentException(s"invalid typeArgs: $typeArgs")
    }
    def toSetters(values: Tree): ((Tree, Tree)) => Seq[Tree] = {
      case (left, right) if left.tpe.widen =:= right.tpe.widen =>
        forPrimitive(values)(left, right)
      case (left, right) if left.tpe <:< typeOf[FieldTransform[_, _]] =>
        forConvertible(values)(left, right)
      case (left, right) =>
        throw new IllegalArgumentException(
          s"type inconsistent: ${parser.getColumn(left)}:[${left.tpe}] != $right:[${right.tpe}]")
    }
    val values = q"${TermName(c.freshName("values"))}"
    val setters = pairs map parser.extractArgs flatMap {
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
