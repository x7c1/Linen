package x7c1.wheat.modern.database.selector

import android.database.sqlite.SQLiteDatabase


trait CanProvideSelector[A]{
  type Selector
  def createFrom(db: SQLiteDatabase): Selector
}

class SelectorProvidable[A, S](selector: SQLiteDatabase => S)
  extends CanProvideSelector[A]{

  override type Selector = S

  override def createFrom(db: SQLiteDatabase) = selector(db)
}
