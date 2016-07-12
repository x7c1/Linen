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
          formattedDate = "2016-07-11 22:59:00",
          reportMessage = "2 Sources found",
          pageTitle = "Sample Page",
          pageUrl = "http://example.com/page/id/123"
        ),
        DiscoveredSourceRow(
          sourceTitle = "Sample Feed Foo Bar",
          sourceDescription = "Sample description.",
          sourceUrl = "http://example.com/feed/rss.xml"
        ),
        DiscoveredSourceRow(
          sourceTitle = "Sample Feed Foo Bar",
          sourceDescription = "Sample description.",
          sourceUrl = "http://example.com/feed/atom.xml"
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
  reportMessage: String,
  pageTitle: String,
  pageUrl: String ) extends SourceSearchReportRow

case class DiscoveredSource(
  sourceTitle: String,
  sourceDescription: String,
  sourceUrl: String ) extends SourceSearchReportRow

case class DateLabelRow() extends SourceSearchReportRow

case class NoSourceRow() extends SourceSearchReportRow

case class LoadingErrorRow() extends SourceSearchReportRow