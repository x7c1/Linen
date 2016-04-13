package x7c1.linen.modern.init.updater

import x7c1.linen.database.struct.{SourceRecord, EntryParts}
import x7c1.linen.database.Updatable
import x7c1.wheat.macros.database.TypedFields


class LoadedSource(
  val sourceId: Long,
  val title: String,
  val description: String,
  entries: Seq[Either[InvalidEntry, EntryParts]]){

  lazy val validEntries = {
    entries collect {
      case Right(x) => x
    } sortWith {
      _.createdAt.timestamp > _.createdAt.timestamp
    }
  }
  lazy val invalidEntries = entries collect { case Left(x) => x }

  def isModifiedFrom(source: InspectedSource): Boolean = {
    (source.title != title) ||
    (source.description != description)
  }
}

object LoadedSource {
  import SourceRecord.column

  implicit object updatable extends Updatable[LoadedSource]{
    override def tableName = SourceRecord.table

    override def toContentValues(target: LoadedSource) = {
      TypedFields toContentValues (
        column.title -> target.title,
        column.description -> target.description
      )
    }
    override def where(target: LoadedSource) = Seq(
      "_id" -> target.sourceId.toString
    )
  }
}
