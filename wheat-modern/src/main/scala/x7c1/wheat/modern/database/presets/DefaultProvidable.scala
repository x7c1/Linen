package x7c1.wheat.modern.database.presets

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.{SelectorProvidable, RecordIdentifiable}

import scala.language.higherKinds

class DefaultProvidable[I[T] <: RecordIdentifiable[T], A]
  extends SelectorProvidable[A, DefaultSelector[I, A]](new DefaultSelector(_))

class DefaultSelector[I[T] <: RecordIdentifiable[T], A](val db: SQLiteDatabase)
  extends Find[I, A] with CollectFrom[I, A]
