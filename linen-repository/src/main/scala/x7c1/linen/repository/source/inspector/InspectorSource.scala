package x7c1.linen.repository.source.inspector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{HasSourceId, HasSourceUrl, SourceRecord}
import x7c1.wheat.modern.database.selector.presets.{CanFindEntity, FindBy}
import x7c1.wheat.modern.database.selector.{CursorConvertible, SelectorProvidable}

case class InspectorSource(sourceId: Long)

object InspectorSource {

  implicit object id extends HasSourceId[InspectorSource] {
    override def toId = _.sourceId
  }

  implicit object readable extends CursorConvertible[SourceRecord, InspectorSource] {
    override def convertFrom = record =>
      new InspectorSource(
        sourceId = record._id
      )
  }

  implicit object findable extends CanFindEntity[HasSourceUrl, SourceRecord, InspectorSource]

  implicit object providable extends SelectorProvidable[InspectorSource, Selector]

  class Selector(
    protected val db: SQLiteDatabase) extends FindBy[HasSourceUrl, InspectorSource]

}
