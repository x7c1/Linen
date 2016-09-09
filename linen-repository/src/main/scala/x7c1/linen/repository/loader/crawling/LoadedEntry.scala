package x7c1.linen.repository.loader.crawling

import x7c1.linen.database.struct.EntryParts
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl

case class LoadedEntry(
  title: String,
  content: String,
  author: String,
  url: EntryUrl,
  createdAt: Date
)

object LoadedEntry {

  def toEntryParts(sourceId: Long): LoadedEntry => EntryParts =
    entry => EntryParts(
      sourceId = sourceId,
      title = entry.title,
      content = entry.content,
      author = entry.author,
      url = entry.url,
      createdAt = entry.createdAt
    )

}
