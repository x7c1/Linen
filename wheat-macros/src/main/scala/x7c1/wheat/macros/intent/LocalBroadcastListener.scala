package x7c1.wheat.macros.intent

import android.content.{Intent, BroadcastReceiver, Context, IntentFilter}
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import x7c1.wheat.macros.base.TreeContext

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/*
case class SomeEvent(foo: Long)

lazy val listener = LocalBroadcastListener[SomeEvent]{
  event => println(event.foo)
}

is expanded like below:

lazy val listener = {
  val receiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) = try {
      val event = {
        val x1 = if (intent.hasExtra(""com.example.SomeEvent.foo"")) {
          intent.getLongExtra("com.example.SomeEvent.foo", -1)
        } else {
          throw new ExtraNotFoundException("com.example.SomeEvent.foo")
        }
        new SomeEvent(x1)
      }
      val f = (event: com.example.SomeEvent) => {
        println(event.foo)
      }
      f(event)
    } catch {
      case e: ExtraNotFoundException =>
        Log.e("com.example.FooActivity", s"[${e.key}] not found in received Intent")
    }
  }
  val filter = new IntentFilter("com.example.SomeEvent")
  new LocalBroadcastListener(receiver, filter)
}
*/

object LocalBroadcastListener {
  def apply[A](block: A => Unit): LocalBroadcastListener =
    macro LocalBroadcastListenerImpl.create[A]
}

class LocalBroadcastListener (receiver: BroadcastReceiver, filter: IntentFilter){
  def registerTo(context: Context): Unit = {
    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
  }
  def unregisterFrom(context: Context): Unit = {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
  }
}

object LocalBroadcastListenerImpl {
  def create[A: c.WeakTypeTag](c: blackbox.Context)(block: c.Tree): c.Tree = {
    import c.universe._
    val factory = new LocalBroadcastListenerFactory {
      override val context: c.type = c
      override val blockTree = block
      override val eventType = weakTypeOf[A]
    }
    val tree = factory.instantiate()
//    println(tree)
    tree
  }
}

private trait LocalBroadcastListenerFactory
  extends TreeContext with PublicFieldsFinder {

  import context.universe._
  val blockTree: Tree
  val eventType: Type

  def instantiate() = {
    val Seq(receiver, filter) = createTermNames("receiver", "filter")
    q"""
      val $receiver = $createReceiver
      val $filter = $createFilter
      new ${typeOf[LocalBroadcastListener]}(
        receiver = $receiver,
        filter = $filter
      )
    """
  }
  def createReceiver = {
    val Seq(context, intent, event, f, e, message) =
      createTermNames("context", "intent", "event", "f", "e", "message")

    q"""
      new ${typeOf[BroadcastReceiver]}{
        override def onReceive($context: ${typeOf[Context]}, $intent: ${typeOf[Intent]}) = {
          try {
            val $event = ${createEvent(intent)}
            val $f = $blockTree
            $f($event)
          } catch {
            case $e: ${typeOf[ExtraNotFoundException]} =>
              val $message =
                s"[$${$e.key}] not found in received Intent" +:
                $e.getStackTrace take 10 mkString "\n"

              ${typeOf[Log].companion}.e(${enclosing.fullName}, $message)
          }
        }
      }
    """
  }
  def createFilter = {
    q"""new ${typeOf[IntentFilter]}(${eventType.typeSymbol.fullName})"""
  }
  def createEvent(intent: TermName): Tree = {
    val pairs = findConstructorOf(eventType).
      map(_.paramLists flatMap {_ map toGet(intent)}).
      getOrElse(List()).
      map { TermName(context freshName "x") -> _ }

    val tmps = pairs map { case (x, get) => q"val $x = $get" }
    val args = pairs map { _._1 }
    q"""
      ..$tmps
      new $eventType(..$args)
    """
  }
  def toGet(intent: TermName)(param: Symbol): Tree = {
    val key = param.fullName
    val tree = param.typeSignatureIn(eventType) match {
      case x if x =:= typeOf[Long] =>
        q"$intent.getLongExtra($key, -1)"
      case x if x =:= typeOf[Boolean] =>
        q"$intent.getBooleanExtra($key, false)"
      case x if x <:< typeOf[Serializable] =>
        q"$intent.getSerializableExtra($key).asInstanceOf[$x]"
      case x =>
        throw new IllegalArgumentException(s"unsupported type : $x")
    }
    q"""
      if ($intent.hasExtra($key)){
        $tree
      } else {
        throw new ${typeOf[ExtraNotFoundException]}($key)
      }
    """
  }
}

trait PublicFieldsFinder extends TreeContext {
  import context.universe._

  def findConstructorOf(targetType: Type): Option[MethodSymbol] = {
    targetType.members.
      filter(_.isConstructor).map(_.asMethod).
      find(_.paramLists exists (_.nonEmpty))
  }
}
