package x7c1.wheat.macros.intent

import android.content.{Context, Intent}
import android.os.Bundle

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

    val factory = new ServiceCallerTreeFactory {
      override val universe: c.universe.type = c.universe
      override val block = f
    }
    val intent = TermName(c freshName "intentss")
    val tree = q"""{
      val $intent = new ${typeOf[Intent]}($context, $klass)
      $intent.setAction(${factory.methodName})
      ..${factory putExtras intent}
      $context.startService($intent)
    }"""

    println(showCode(tree))
    tree
  }
}

private trait ServiceCallerTreeFactory {
  val universe: Universe
  import universe._

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

}

trait BundleConvertible[A] {
  def toBundle(target: A): Bundle
}
