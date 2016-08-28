package x7c1.wheat.modern.queue.map

trait ValueQueue[V] {

  /**
    * return true if previous value exists
    */
  def enqueue(value: V): Boolean

  /*
   * return Some if next value exists, otherwise None
   */
  def dequeue(value: V): Option[V]
}

object ValueQueue {

  def toDistribute[K, V](getGroupKey: V => K): ValueQueue[V] = {
    new GroupingQueue[K, V](getGroupKey)
  }
}

private class GroupingQueue[K, V](getGroupKey: V => K) extends ValueQueue[V] {

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
