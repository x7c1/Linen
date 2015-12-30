package x7c1.wheat.modern.observer.recycler

sealed trait DragDirection

case object Next extends DragDirection

case object Previous extends DragDirection

object DragDirection {
  def create(distance: Float): Option[DragDirection] = {
    if (distance > 0) Some(Previous)
    else if (distance < 0) Some(Next)
    else None
  }
}
