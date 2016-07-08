package x7c1.linen.repository.inspector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasAccountId
import x7c1.wheat.modern.database.selector.SelectorProvidable
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequence, TraverseOn}
import x7c1.wheat.modern.sequence.Sequence

sealed trait InspectorReportRow

object InspectorReportRow {
  implicit object traverse extends CanTraverse[HasAccountId, InspectorReportRow]{
    override def extract[X: HasAccountId](db: SQLiteDatabase, id: X) = {
      val ys = Sequence from (0 until 10).map{ n =>
        DiscoveredSource(s"source-$n")
      }
      val xs = new ClosableSequence[InspectorReportRow] {
        override def closeCursor(): Unit = {}
        override def findAt(position: Int) = ys findAt position
        override def length = ys.length
      }
      Right(xs)
    }
  }
  implicit object providable extends SelectorProvidable[InspectorReportRow, Selector]

  class Selector(
    protected val db: SQLiteDatabase) extends TraverseOn[HasAccountId, InspectorReportRow]
}

case class DiscoveredSourceLabel(
  body: String ) extends InspectorReportRow

case class DiscoveredSource(
  label: String ) extends InspectorReportRow
