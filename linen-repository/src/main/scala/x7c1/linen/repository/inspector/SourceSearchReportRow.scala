package x7c1.linen.repository.inspector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasAccountId
import x7c1.wheat.modern.database.selector.SelectorProvidable
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequence, TraverseOn}
import x7c1.wheat.modern.sequence.Sequence

sealed trait SourceSearchReportRow

object SourceSearchReportRow {
  implicit object traverse extends CanTraverse[HasAccountId, SourceSearchReportRow]{
    override def extract[X: HasAccountId](db: SQLiteDatabase, id: X) = {
      val ys = Sequence from Seq(
        DiscoveredLabelRow(
          formattedDate = "Mon Jul 11",
          message = "2 sources found",
          via = "http://example.com/page/id/123"
        ),
        DiscoveredSourceRow(
          title = "Sample Feed Foo Bar",
          url = "http://example.com/feed/rss.xml"
        ),
        DiscoveredSourceRow(
          title = "Sample Feed Foo Bar",
          url = "http://example.com/feed/atom.xml"
        )
      )
      val xs = new ClosableSequence[SourceSearchReportRow] {
        override def closeCursor(): Unit = {}
        override def findAt(position: Int) = ys findAt position
        override def length = ys.length
      }
      Right(xs)
    }
  }
  implicit object providable extends SelectorProvidable[SourceSearchReportRow, Selector]

  class Selector(
    protected val db: SQLiteDatabase) extends TraverseOn[HasAccountId, SourceSearchReportRow]
}

case class DiscoveredLabelRow(
  formattedDate: String,
  message: String,
  via: String ) extends SourceSearchReportRow

case class DiscoveredSourceRow(
  title: String,
  url: String ) extends SourceSearchReportRow

case class DateLabelRow() extends SourceSearchReportRow

case class NoSourceRow() extends SourceSearchReportRow

case class LoadingErrorRow() extends SourceSearchReportRow
