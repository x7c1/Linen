package x7c1.wheat.modern.database.selector.presets

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.selector.{CanIdentify, SelectorProvidable}

import scala.language.higherKinds

class DefaultProvidable[I[T] <: CanIdentify[T], A]
  extends SelectorProvidable[A, DefaultSelector[I, A]](new DefaultSelector(_))

class DefaultSelector[I[T] <: CanIdentify[T], A](val db: SQLiteDatabase)
  extends Find[A] with FindBy[I, A] with CollectFrom[I, A]
