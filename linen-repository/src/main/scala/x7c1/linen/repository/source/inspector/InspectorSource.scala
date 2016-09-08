package x7c1.linen.repository.source.inspector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{HasSourceUrl, SourceRecord}
import x7c1.wheat.modern.database.selector.presets.{CanFindEntity, FindBy}
import x7c1.wheat.modern.database.selector.{CursorConvertible, SelectorProvidable}

case class InspectorSource(original: SourceRecord)

object InspectorSource {

  implicit object readable extends CursorConvertible[SourceRecord, InspectorSource] {
    override def convertFrom = new InspectorSource(_)
  }
  implicit object findable extends CanFindEntity[HasSourceUrl, SourceRecord, InspectorSource]

  implicit object providable extends SelectorProvidable[InspectorSource, Selector]

  class Selector(
    protected val db: SQLiteDatabase) extends FindBy[HasSourceUrl, InspectorSource]
}
