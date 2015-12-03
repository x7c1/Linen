package x7c1.linen.modern.accessor

trait SourceStateAccessor {
  def findState(sourceId: Long): Option[SourceState]
}

class SourceStateBuffer extends SourceStateAccessor {
  import scala.collection.mutable

  private val cache: mutable.Map[Long, SourceState] = mutable.Map()

  override def findState(sourceId: Long): Option[SourceState] = {
    cache get sourceId
  }
  def updateState(sourceId: Long, state: SourceState): Unit = {
    cache(sourceId) = state
  }
}

sealed trait SourceState

object SourceLoading extends SourceState

object SourcePrefetched extends SourceState
