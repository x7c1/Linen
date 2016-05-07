package x7c1.linen.repository.unread

import x7c1.linen.repository.entry.unread.{EntryAccessor, UnreadDetail, UnreadOutline}
import x7c1.linen.repository.source.unread.{RawSourceAccessor, UnreadSourceAccessor}

class Accessors(
  val source: UnreadSourceAccessor,
  val entryOutline: EntryAccessor[UnreadOutline],
  val entryDetail: EntryAccessor[UnreadDetail],
  val rawSource: RawSourceAccessor
)
