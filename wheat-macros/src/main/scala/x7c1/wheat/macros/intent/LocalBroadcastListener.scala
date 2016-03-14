package x7c1.wheat.macros.intent

import android.content.{Intent, BroadcastReceiver, Context, IntentFilter}
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import x7c1.wheat.macros.base.TreeContext

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


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
    println(tree)
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
    val Seq(context, intent, event, f, e) =
      createTermNames("context", "intent", "event", "f", "e")

    q"""
      new ${typeOf[BroadcastReceiver]}{
        override def onReceive($context: ${typeOf[Context]}, $intent: ${typeOf[Intent]}) = {
          try {
            val $event = ${createEvent(intent)}
            val $f = $blockTree
            $f($event)
          } catch {
            case e: ${typeOf[ExtraNotFoundException]} =>
              ${typeOf[Log].companion}.i("hoge", "fuga")
          }
        }
      }
    """
  }
  def createFilter = {
    q"""new ${typeOf[IntentFilter]}(${eventType.typeSymbol.fullName})"""
  }
  def createEvent(intent: TermName): Tree = {
    val putExtras = findConstructorOf(eventType).
      map(_.paramLists flatMap {_ map toGet(intent)}).
      getOrElse(List())

    q"new $eventType(..$putExtras)"
  }
  def toGet(intent: TermName)(param: Symbol): Tree = {
    val key = param.name.toString
    param.typeSignatureIn(eventType) match {
      case x if x =:= typeOf[Long] =>
        q"$intent.getLongExtra($key, -1)"
      case x if x =:= typeOf[Boolean] =>
        q"$intent.getBooleanExtra($key, false)"
      case x if x <:< typeOf[Serializable] =>
        q"$intent.getSerializableExtra($key).asInstanceOf[$x]"
      case x =>
        throw new IllegalArgumentException(s"unsupported type : $x")
    }
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
