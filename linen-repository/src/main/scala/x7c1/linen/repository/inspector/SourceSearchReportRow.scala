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
        DiscoveredSourceLabel(
          formattedDate = "2016-07-11 22:59:00",
          reportMessage = "2 Sources found.",
          pageTitle = "Sample Page",
          pageUrl = "http://example.com/page/id/123"
        ),
        DiscoveredSource(
          sourceTitle = "Sample Feed Foo Bar",
          sourceDescription = "Sample description.",
          sourceUrl = "http://example.com/feed/rss.xml"
        ),
        DiscoveredSource(
          sourceTitle = "Sample Feed Foo Bar",
          sourceDescription = "Sample description.",
          sourceUrl = "http://example.com/feed/atom.xml"
        ),
        UrlLoadingErrorLabel(
          formattedDate = "2016-07-10 22:19:00",
          reportMessage = "Discovery failed.",
          pageUrl = "http://example.com/page/id/123?foo=bar"
        ),
        UrlLoadingError(
          errorText = "HTML Parse Error",
          pageUrl = "http://example.com/page/id/123?foo=bar&foo=bar1&foo=bar2&foo=bar3&foo=bar4&foo=bar5&foo=bar6&foo=bar7&foo=bar8&foo=bar9&foo=bar10&foo=bar11&end"
        ),
        NoSourceFoundLabel(
          formattedDate = "2016-07-09 21:15:00",
          reportMessage = "Discovery failed",
          pageUrl = "http://example.com/page/id/123"
        ),
        NoSourceFound(
          pageTitle = "Example Page",
          pageUrl = "http://example.com/page/id/123",
          reportMessage = "Source Not Found"
        ),
        DiscoveredSourceLabel(
          formattedDate = "2016-07-09 21:15:00",
          reportMessage = "1 Source found.",
          pageTitle = "Sample Page",
          pageUrl = "http://example.com/page/id/123"
        ),
        SourceLoadingError(
          errorText = "Connection Timeout",
          pageUrl = "http://example.com/feed/atom.xml"
        ),
        Footer()
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

case class DiscoveredSourceLabel(
  formattedDate: String,
  reportMessage: String,
  pageTitle: String,
  pageUrl: String ) extends SourceSearchReportRow

case class DiscoveredSource(
  sourceTitle: String,
  sourceDescription: String,
  sourceUrl: String ) extends SourceSearchReportRow

case class UrlLoadingErrorLabel(
  formattedDate: String,
  reportMessage: String,
  pageUrl: String ) extends SourceSearchReportRow

case class UrlLoadingError(
  errorText: String,
  pageUrl: String ) extends SourceSearchReportRow

case class SourceLoadingError(
  errorText: String,
  pageUrl: String ) extends SourceSearchReportRow

case class NoSourceFoundLabel(
  formattedDate: String,
  reportMessage: String,
  pageUrl: String ) extends SourceSearchReportRow

case class NoSourceFound(
  pageTitle: String,
  pageUrl: String,
  reportMessage: String ) extends SourceSearchReportRow

case class Footer() extends SourceSearchReportRow