package x7c1.wheat.modern.queue.map

import scala.collection.mutable

trait QueueMap[K, V] {

  def has(key: K): Boolean

  def length(key: K): Int

  def enqueue(value: V): Unit

  def dequeue(key: K): Option[V]

  def headOption(key: K): Option[V]
}

object QueueMap {
  def apply[K, V](getKey: V => K): QueueMap[K, V] = new QueueMapImpl[K, V](getKey)
}

private class QueueMapImpl[K, V](getKey: V => K) extends QueueMap[K, V] {

  private val map = mutable.Map[K, mutable.Queue[V]]()

  override def has(key: K): Boolean = synchronized {
    map get key exists (_.nonEmpty)
  }

  override def length(key: K): Int = synchronized {
    map get key map (_.length) getOrElse 0
  }

  override def enqueue(value: V): Unit = synchronized {
    val key = getKey(value)
    map.getOrElseUpdate(key, mutable.Queue()) enqueue value
  }

  override def dequeue(key: K): Option[V] = synchronized {
    map get key match {
      case Some(queue) if queue.isEmpty =>
        map remove key
        None
      case Some(queue) =>
        val value = queue.dequeue()
        if (queue.isEmpty) {
          map remove key
        }
        Some(value)
      case None =>
        None
    }
  }

  override def headOption(key: K): Option[V] = synchronized {
    map.get(key).flatMap(_.headOption)
  }
}
