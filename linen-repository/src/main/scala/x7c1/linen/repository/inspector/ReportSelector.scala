package x7c1.linen.repository.inspector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.InspectorStatusRecord
import x7c1.linen.database.struct.InspectorLoadingStatus.{ConnectionTimeout, ParseError, UnknownError, UnknownHostError}
import x7c1.linen.database.struct.{HasAccountId, InspectorLoadingStatus}
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, TraverseOn}
import x7c1.wheat.modern.features.HasShortLength
import x7c1.wheat.modern.sequence.HeadlineSequencer

class ReportSelector(
  protected val db: SQLiteDatabase) extends TraverseOn[HasAccountId, SourceSearchReportRow]

trait TraverseReport extends CanTraverse[HasAccountId, SourceSearchReportRow] {
  override def extract[X: HasAccountId](db: SQLiteDatabase, id: X) = {

    implicit val short = HasShortLength[InspectorStatusRecord]

    val sequencer = HeadlineSequencer[InspectorStatusRecord, SourceSearchReportRow](
      equals = _.action_id == _.action_id,
      toHeadline = records =>
        DiscoveredSourceLabel(
          formattedDate = {
            records findAt 0 map (_.created_at.typed.format) getOrElse ""
          },
          reportMessage = s"${records.length} sources found.",
          pageTitle = "",
          pageUrl = ""
        )
    )

    for {
      records <- db.selectorOf[InspectorStatusRecord].traverseOn(id).right
    } yield {
      sequencer derive records map {
        case Left(row) => row
        case Right(record) => new SourceSearchReportRowFactory(record).create
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

private class SourceSearchReportRowFactory(record: InspectorStatusRecord) {

  def create: SourceSearchReportRow = {
    val either = for {
      sourceUrl <- {
        lazy val row = forNoSource(record.action_loading_status.typed)
        record.latent_source_url.toRight(row).right
      }
    } yield {
      forLatentSourceUrl(sourceUrl)(record.discovered_source_id)
    }
    either.merge
  }

  private def forNoSource(status: InspectorLoadingStatus) = {
    status match {
      case UnknownHostError =>
        ClientLoadingError(
          errorText = s"Unknown Host",
          pageUrl = record.origin_url
        )
      case ParseError =>
        OriginLoadingError(
          errorText = s"Parse Error",
          pageUrl = record.origin_url
        )
      case ConnectionTimeout =>
        ClientLoadingError(
          pageUrl = record.origin_url,
          errorText = s"Source Not Found (Connection Timeout)"
        )
      case UnknownError =>
        NoSourceFound(
          pageTitle = "",
          pageUrl = record.origin_url,
          reportMessage = s"Source Not Found (Unknown Error)"
        )
      case _ =>
        NoSourceFound(
          pageTitle = "",
          pageUrl = record.origin_url,
          reportMessage = s"Source Not Found (Unexpected Status)"
        )
    }
  }

  private def forLatentSourceUrl(sourceUrl: String): Option[Long] => SourceSearchReportRow = {
    case Some(sourceId) =>
      DiscoveredSource(
        sourceId = sourceId,
        sourceTitle = record.source_title getOrElse "",
        sourceDescription = s"description of ${record.latent_source_url}",
        sourceUrl = sourceUrl
      )
    case None =>
      ClientLoadingError(
        errorText = s"Loading Error : ${record.origin_title}",
        pageUrl = sourceUrl
      )
  }
}
