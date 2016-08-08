package x7c1.linen.repository.inspector

import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.modern.kinds.Fate

trait PageInspector {
  def inspect(pageUrl: String): Fate[CrawlerContext, InspectorError, DiscoveredResource]
}

trait InspectorError

trait DiscoveredResource {
  def latentSourceUrl: String
}

