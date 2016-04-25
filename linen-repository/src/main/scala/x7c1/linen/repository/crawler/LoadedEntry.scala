package x7c1.linen.repository.crawler

import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl

case class LoadedEntry(
  title: String,
  content: String,
  author: String,
  url: EntryUrl,
  createdAt: Date
)
