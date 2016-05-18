package x7c1.linen.repository.loader.crawling

import java.io.{BufferedInputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.{SyndEntry, SyndFeed}
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.TaskProvider.using

import scala.concurrent.{ExecutionContext, Future}

trait SourceLoader {
  def loadSource(source: InspectedSource)(implicit x: ExecutionContext): Future[LoadedSource]
}

object RemoteSourceLoader extends SourceLoader {
  override def loadSource(source: InspectedSource)(implicit x: ExecutionContext): Future[LoadedSource] = {
    val loader = source.feedUrl.getHost match {
      case host if host endsWith "example.com" => ExampleLoader
      case _ => RealLoader
    }
    loader loadSource source
  }
}

private object ExampleLoader extends SourceLoader {
  override def loadSource(source: InspectedSource)(implicit x: ExecutionContext) = {
    Log info s"[init] $source"
    Future {
      new LoadedSource(
        sourceId = source.sourceId,
        title = source.title,
        description = source.description,
        entries = createEntries()
      )
    }
  }
  private def createEntries(): Seq[Either[InvalidEntry, LoadedEntry]] = {
    Seq()
  }
}

private object RealLoader extends SourceLoader {
  import collection.JavaConverters._

  override def loadSource(source: InspectedSource)(implicit x: ExecutionContext) = {
    Future(source.feedUrl).map(loadRawFeed).flatMap(_.toFuture) map { feed =>
      val entries = feed.getEntries.asScala map { case x: SyndEntry => x }
      new LoadedSource(
        sourceId = source.sourceId,
        title = Option(feed.getTitle) getOrElse "",
        description = Option(feed.getDescription) getOrElse "",
        entries = entries map convertEntry
      )
    }
  }
  private def loadRawFeed(feedUrl: URL): CallbackTask[SyndFeed] = {
    for {
      connection <- task {
        val connection = feedUrl.openConnection().asInstanceOf[HttpURLConnection]
        connection setRequestMethod "GET"
        connection
      }
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      new SyndFeedInput().build(reader)
    }
  }
  private def convertEntry(entry: SyndEntry): Either[InvalidEntry, LoadedEntry] = {
    try for {
      url <- (Option(entry.getLink) toRight EmptyUrl()).right
      published <- (Option(entry.getPublishedDate) toRight EmptyPublishedDate()).right
    } yield LoadedEntry(
        title = Option(entry.getTitle) getOrElse "",
        content = Option(entry.getDescription.getValue) getOrElse "",
        author = Option(entry.getAuthor) getOrElse "",
        url = EntryUrl(url),
        createdAt = Date(published)
      ) catch {
      case e: Exception => Left(Abort(e))
    }
  }
}
