package x7c1.wheat.macros.intent

import android.content.{Intent, Context}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object IntentFactory {
  def using[A]: IntentFactory[A] = new IntentFactory[A]
}

class IntentFactory[A]{
  def create(context: Context, klass: Class[_])(f: A => Unit): Intent =
    macro IntentFactoryImpl.createIntent
}

private object IntentFactoryImpl {
  def createIntent(c: blackbox.Context)
      (context: c.Tree, klass: c.Tree)(f: c.Tree): c.Tree = {

    val factory = new IntentTreeFactory {
      override val context: c.type = c
      override val block = f
    }
    val tree = factory.newIntent(context, klass)
//    println(c.universe.showCode(tree))
    tree
  }
}

private trait IntentTreeFactory {
  val context: blackbox.Context
  import context.universe._
  val block: Tree

  case class IntentExtra(
    key: String,
    value: Tree,
    typeName: String
  )
  private lazy val (_, call) = block.children match {
    case Seq(x, y: Apply) => x -> y
    case _ => throw new IllegalArgumentException("invalid form of expression")
  }
  lazy val methodName: String = call.symbol.asMethod.fullName

  private def extras: List[IntentExtra] = {
    val pairs = call.symbol.asMethod.paramLists match {
      case xs if xs.length > 1 =>
        throw new IllegalArgumentException(s"too many paramLists : $xs")
      case Seq(params) =>
        params map { param =>
          param.name.encodedName.toString ->
            param.typeSignature.typeSymbol.fullName
        }
    }
    pairs zip call.children.tail map {
      case ((paramName, paramType), arg) =>
        IntentExtra(
          key = s"$methodName.$paramName",
          value = arg,
          typeName = paramType )
    }
  }
  def putExtras(intent: TermName): List[Tree] = extras map { extra =>
    q"$intent.putExtra(${extra.key}, ${extra.value})"
  }
  def newIntent(androidContext: Tree, klass: Tree): Tree = {
    val intent = TermName(context freshName "intent")
    q"""{
      val $intent = new ${typeOf[Intent]}($androidContext, $klass)
      $intent.setAction($methodName)
      ..${putExtras(intent)}
      $intent
    }"""
  }
}
