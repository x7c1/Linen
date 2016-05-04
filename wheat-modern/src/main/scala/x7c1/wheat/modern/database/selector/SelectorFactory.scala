package x7c1.wheat.modern.database.selector

import android.database.sqlite.SQLiteDatabase


trait SelectorFactory[A]{
  type Selector
  def createFrom(db: SQLiteDatabase): Selector
}

class SelectorProvidable[A, S](selector: SQLiteDatabase => S)
  extends SelectorFactory[A]{

  override type Selector = S

  override def createFrom(db: SQLiteDatabase) = selector(db)
}
