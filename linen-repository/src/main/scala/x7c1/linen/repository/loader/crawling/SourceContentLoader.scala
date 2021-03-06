package x7c1.linen.repository.loader.crawling

import java.io.Reader
import java.net.URL

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.{SyndEntry, SyndFeed}
import com.google.code.rome.android.repackaged.com.sun.syndication.io.{ParsingFeedException, SyndFeedInput}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.loader.crawling.SourceContentLoaderError.{LoaderParseError, UnexpectedError}
import x7c1.wheat.modern.fate.HttpRequester
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.kinds.Fate

object SourceContentLoader {
  def apply(): SourceContentLoader = new SourceContentLoaderImpl
}

trait SourceContentLoader {
  def loadContent(url: URL): Fate[CrawlerContext, SourceContentLoaderError, SourceContent]
}

private class SourceContentLoaderImpl extends SourceContentLoader {

  import collection.JavaConverters._

  override def loadContent(url: URL) = {
    val requester = HttpRequester[CrawlerContext, SourceContentLoaderError]()
    requester readerOf url flatMap toFeed map { feed =>
      SourceContent(
        title = Option(feed.getTitle) getOrElse "",
        entries = {
          val entries = feed.getEntries.asScala map { case x: SyndEntry => x }
          entries map convertEntry(url)
        },
        description = Option(feed.getDescription) getOrElse ""
      )
    }
  }

  private def toFeed(reader: Reader): Fate[CrawlerContext, SourceContentLoaderError, SyndFeed] = {
    try {
      Fate(new SyndFeedInput() build reader)
    } catch {
      case e: ParsingFeedException =>
        Fate left LoaderParseError(
          detail = format(e)("[parse-error]"),
          cause = Some(e)
        )
      case e: Throwable =>
        Fate left UnexpectedError(e)
    }
  }

  private def convertEntry(url: URL)(entry: SyndEntry): Either[InvalidEntry, LoadedEntry] = {
    try for {
      url <- {
        val either = Option(entry.getLink) toRight EmptyUrl(url)
        either.right
      }
      published <- {
        val either = Option(entry.getPublishedDate) toRight EmptyPublishedDate(new URL(url))
        either.right
      }
    } yield LoadedEntry(
      title = Option(entry.getTitle) getOrElse "",
      content = Option(entry.getDescription) map (_.getValue) getOrElse "",
      author = Option(entry.getAuthor) getOrElse "",
      url = EntryUrl(url),
      createdAt = Date(published)
    ) catch {
      case e: Exception => Left(Abort(e))
    }
  }
}

case class SourceContent(
  title: String,
  description: String,
  entries: Seq[Either[InvalidEntry, LoadedEntry]]
)
