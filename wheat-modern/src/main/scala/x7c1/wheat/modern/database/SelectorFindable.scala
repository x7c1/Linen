package x7c1.wheat.modern.database

import scala.language.higherKinds

trait SelectorFindable[A]{
  type Selector
  def selectorFrom(db: ReadableDatabase): Selector
}

class SingleSelectorFindable[A, S](selector: ReadableDatabase => S)
  extends SelectorFindable[A]{

  override type Selector = S

  override def selectorFrom(db: ReadableDatabase) = selector(db)
}

class UniqueSelector[I[T] <: RecordIdentifiable[T], A](db: ReadableDatabase){
  def from[X: I](x: X)(implicit i: MultipleSelectable2[I, A]): i.Result[A] = {
    db.select2[A] by x
  }
}

class UniqueSelectorFindable[I[T] <: RecordIdentifiable[T], A]
  extends SingleSelectorFindable[A, UniqueSelector[I, A]](
    readable => new UniqueSelector(readable)
  )
