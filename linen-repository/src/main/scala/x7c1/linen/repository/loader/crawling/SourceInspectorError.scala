package x7c1.linen.repository.loader.crawling

import android.database.SQLException
import x7c1.linen.database.struct.HasSourceId
import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

sealed trait SourceInspectorError {
  def message: String
}

object SourceInspectorError {
  implicit object unexpected extends HasConstructor[Throwable => SourceInspectorError]{
    override def newInstance = UnexpectedError(_)
  }
  case class SourceNotFound[A: HasSourceId](source: A) extends SourceInspectorError {
    private val sourceId = implicitly[HasSourceId[A]] toId source
    override def message: String = s"source(id:$sourceId) not found"
  }

  case class SqlError(e: SQLException) extends SourceInspectorError {
    override def message: String = e.getMessage
  }

  case class UnexpectedError(e: Throwable) extends SourceInspectorError {
    override def message: String = format(e){"[failed] unexpected error"}
  }
}
