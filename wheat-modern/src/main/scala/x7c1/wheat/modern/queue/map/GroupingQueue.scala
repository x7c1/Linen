package x7c1.wheat.modern.queue.map

trait GroupingQueue[V] {

  /**
    * return true if the group of enqueued value already exists,
    * otherwise false
    */
  def enqueue(value: V): Boolean

  /**
    * return Some if the next value exists in the group of `value`,
    * otherwise None
    */
  def dequeue(value: V): Option[V]
}

object GroupingQueue {

  def groupBy[K, V](getGroupKey: V => K): GroupingQueue[V] = {
    new GroupingQueueImpl[K, V](getGroupKey)
  }
}

private class GroupingQueueImpl[K, V](getGroupKey: V => K) extends GroupingQueue[V] {

  private val queueMap = QueueMap[K, V](getGroupKey)

  override def dequeue(value: V): Option[V] = synchronized {
    val key = getGroupKey(value)
    queueMap dequeue key
    queueMap headOption key
  }

  override def enqueue(value: V): Boolean = synchronized {
    val exists = queueMap has getGroupKey(value)
    queueMap enqueue value
    exists
  }
}
