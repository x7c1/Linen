package x7c1.wheat.modern.database.presets

import android.database.SQLException
import x7c1.wheat.modern.database.{Findable2, ReadableDatabase, RecordIdentifiable, SeqSelectable2}
import x7c1.wheat.modern.either.OptionEither
import x7c1.wheat.modern.either.Imports._

import scala.language.higherKinds

trait CollectFrom [I[T] <: RecordIdentifiable[T], A]{
  protected def readable: ReadableDatabase

  def collectFrom[X: I](target: X)(implicit i: SeqSelectable2[I, A]): Either[SQLException, Seq[A]] = {
    readable.select2[Seq[A]] by target
  }
}

trait Find[I[T] <: RecordIdentifiable[T], A]{
  protected def readable: ReadableDatabase

  def find[X: I](target: X)(implicit i: Findable2[I, A]): OptionEither[SQLException, A] = {
    val either = readable.select2[Option[A]] by target
    either.toOptionEither
  }
}
