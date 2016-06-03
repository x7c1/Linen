package x7c1.wheat.modern.database.selector

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.selector.SelectorProvidable.CanReify

import scala.language.reflectiveCalls


trait CanProvideSelector[A]{
  type Selector
  def createFrom(db: SQLiteDatabase): Selector
}

class SelectorProvidable[A, S: CanReify] extends CanProvideSelector[A]{
  override type Selector = S
  override def createFrom(db: SQLiteDatabase): S = {
    implicitly[CanReify[S]] apply db
  }
}

object SelectorProvidable {
  object Implicits {
    implicit class SelectorProvidableDatabase(val db: SQLiteDatabase) extends AnyVal {
      def selectorOf[A](implicit x: CanProvideSelector[A]): x.Selector = {
        x createFrom db
      }
    }
  }
  type CanReify[A] = SQLiteDatabase => A
}
