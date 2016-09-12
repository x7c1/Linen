package x7c1.linen.repository.inspector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.InspectorStatusRecord
import x7c1.linen.database.struct.HasAccountId
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequence, TraverseOn}
import x7c1.wheat.modern.features.HasShortLength
import x7c1.wheat.modern.sequence.HeadlineSequencer

class ReportSelector(
  protected val db: SQLiteDatabase) extends TraverseOn[HasAccountId, SourceSearchReportRow]

trait TraverseReport extends CanTraverse[HasAccountId, SourceSearchReportRow] {
  override def extract[X: HasAccountId](db: SQLiteDatabase, id: X) = {

    implicit val short = HasShortLength[InspectorStatusRecord]

    val sequencer = HeadlineSequencer[InspectorStatusRecord, SourceSearchReportRow](
      equals = _.action_id == _.action_id,
      toHeadline = records => {
        val date = records.findAt(0).map(_.created_at.typed.format) getOrElse ""
        DiscoveredSourceLabel(
          formattedDate = date,
          reportMessage = s"${records.length} sources found.",
          pageTitle = "",
          pageUrl = ""
        )
      }
    )

    for {
      records <- db.selectorOf[InspectorStatusRecord].traverseOn(id).right
    } yield {
      val tmp = sequencer.derive(records) map {
        case Right(record) =>
          Log error s"${record.source_title}, ${record.discovered_source_id}"

          DiscoveredSource(
            sourceTitle = s"${record.source_title}",
            sourceDescription = s"description of ${record.latent_source_url}",
            sourceUrl = record.latent_source_url
          )
        case Left(row) => row
      }
      // todo: enable sequencer.derive to support ClosableSequence
      // workaround
      new ClosableSequence[SourceSearchReportRow] {
        override def closeCursor(): Unit = records.closeCursor()

        override def length: Int = tmp.length

        override def findAt(position: Int) = tmp.findAt(position)
      }
    }

    //      val ys = Sequence from Seq(
    //        DiscoveredSourceLabel(
    //          formattedDate = "2016-07-11 22:59:00",
    //          reportMessage = "2 Sources found.",
    //          pageTitle = "Sample Page",
    //          pageUrl = "http://example.com/page/id/123"
    //        ),
    //        DiscoveredSource(
    //          sourceTitle = "Sample Feed Foo Bar",
    //          sourceDescription = "Sample description.",
    //          sourceUrl = "http://example.com/feed/rss.xml"
    //        ),
    //        DiscoveredSource(
    //          sourceTitle = "Sample Feed Foo Bar",
    //          sourceDescription = "Sample description.",
    //          sourceUrl = "http://example.com/feed/atom.xml"
    //        ),
    //        UrlLoadingErrorLabel(
    //          formattedDate = "2016-07-10 22:19:00",
    //          reportMessage = "Discovery failed.",
    //          pageUrl = "http://example.com/page/id/123?foo=bar"
    //        ),
    //        UrlLoadingError(
    //          errorText = "HTML Parse Error",
    //          pageUrl = "http://example.com/page/id/123?foo=bar&foo=bar1&foo=bar2&foo=bar3&foo=bar4&foo=bar5&foo=bar6&foo=bar7&foo=bar8&foo=bar9&foo=bar10&foo=bar11&end"
    //        ),
    //        NoSourceFoundLabel(
    //          formattedDate = "2016-07-09 21:15:00",
    //          reportMessage = "Discovery failed",
    //          pageUrl = "http://example.com/page/id/123"
    //        ),
    //        NoSourceFound(
    //          pageTitle = "Example Page",
    //          pageUrl = "http://example.com/page/id/123",
    //          reportMessage = "Source Not Found"
    //        ),
    //        DiscoveredSourceLabel(
    //          formattedDate = "2016-07-09 21:15:00",
    //          reportMessage = "1 Source found.",
    //          pageTitle = "Sample Page",
    //          pageUrl = "http://example.com/page/id/123"
    //        ),
    //        SourceLoadingError(
    //          errorText = "Connection Timeout",
    //          pageUrl = "http://example.com/feed/atom.xml"
    //        ),
    //        Footer()
    //      )
    //      val xs = new ClosableSequence[SourceSearchReportRow] {
    //        override def closeCursor(): Unit = {}
    //
    //        override def findAt(position: Int) = ys findAt position
    //
    //        override def length = ys.length
    //      }
    //      Right(xs)
  }
}
