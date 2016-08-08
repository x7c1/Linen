package x7c1.linen.repository.inspector

import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.modern.kinds.Fate

trait PageInspector {
  def inspect(pageUrl: String): Fate[CrawlerContext, PageInspectorError, LatentSource]
}

trait PageInspectorError

trait LatentSource {
  def url: String
}

