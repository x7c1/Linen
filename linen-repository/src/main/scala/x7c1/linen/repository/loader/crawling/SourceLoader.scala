package x7c1.linen.repository.loader.crawling

import x7c1.wheat.modern.kinds.Fate

trait SourceLoader {
  def loadSource(source: InspectedSource): Fate[CrawlerContext, SourceLoaderError, LoadedSource]
}

object RemoteSourceLoader extends SourceLoader {
  override def loadSource(source: InspectedSource) = {
    val loader = source.feedUrl.getHost match {
      case host if host endsWith "example.com" => ExampleLoader
      case _ => RealLoader
    }
    loader loadSource source
  }
}

private object ExampleLoader extends SourceLoader {
  override def loadSource(source: InspectedSource) = {
    Fate {
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

  override def loadSource(source: InspectedSource) = {
    SourceContentLoader().loadContent(source.feedUrl).transform {
      case Right(content) =>
        Right(new LoadedSource(
          sourceId = source.sourceId,
          title = content.title,
          description = content.description,
          entries = content.entries
        ))
      case Left(error) =>
        Left(SourceLoaderError.Wrapped(error.cause, error.detail))
    }
  }
}
