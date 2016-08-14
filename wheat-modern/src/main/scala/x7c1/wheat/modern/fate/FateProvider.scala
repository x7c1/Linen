package x7c1.wheat.modern.fate

import java.io.Closeable

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.features.HasInstance
import x7c1.wheat.modern.kinds.Fate

import scala.concurrent.ExecutionContext

object FateProvider {
  type ErrorLike[X] = HasConstructor[Throwable => X]
  type HasContext[X] = HasInstance[X => ExecutionContext]

  def using[X, L, A <: Closeable](closeable: A): Fate[X, L, A] = {
    Fate {
      x => f =>
        try f(Right(closeable))
        finally closeable.close()
    }
  }
}
