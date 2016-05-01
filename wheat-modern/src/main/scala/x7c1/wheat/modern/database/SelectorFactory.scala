package x7c1.wheat.modern.database

import scala.language.higherKinds

trait SelectorFactory[A]{
  type Selector
  def createFrom(db: ReadableDatabase): Selector
}

class SelectorProvidable[A, S](selector: ReadableDatabase => S)
  extends SelectorFactory[A]{

  override type Selector = S

  override def createFrom(db: ReadableDatabase) = selector(db)
}
