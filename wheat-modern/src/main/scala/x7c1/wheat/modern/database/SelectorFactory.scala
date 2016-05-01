package x7c1.wheat.modern.database

import scala.language.higherKinds

trait SelectorFactory[A]{
  type Selector
  def createFrom(db: ReadableDatabase): Selector
}

class SingleSelectorFactory[A, S](selector: ReadableDatabase => S)
  extends SelectorFactory[A]{

  override type Selector = S

  override def createFrom(db: ReadableDatabase) = selector(db)
}

class UniqueSelector[I[T] <: RecordIdentifiable[T], A](db: ReadableDatabase){
  def from[X: I](x: X)(implicit i: MultipleSelectable2[I, A]): i.Result[A] = {
    db.select2[A] by x
  }
}

class UniqueSelectorFactory[I[T] <: RecordIdentifiable[T], A]
  extends SingleSelectorFactory[A, UniqueSelector[I, A]](
    readable => new UniqueSelector(readable)
  )
