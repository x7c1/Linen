package x7c1.wheat.modern.queue.map

trait GroupingQueue[V] {

  /**
    * Enqueue 'value' to its group.
    * Return true if the group already exists, otherwise false.
    */
  def enqueue(value: V): Boolean

  /**
    * Dequeue from the group of 'value'.
    * Return Some if next value exists in its group, otherwise None.
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
