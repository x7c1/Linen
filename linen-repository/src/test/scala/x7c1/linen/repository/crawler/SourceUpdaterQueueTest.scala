package x7c1.linen.repository.crawler

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{SourceParts, retrieved_source_marks}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.{DummyEntryBinder, DummySourceLoader}
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.modern.either.OptionRight

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SourceUpdaterQueueTest extends JUnitSuiteLike {
  import DummySourceLoader.Implicits._

  @Test
  def testLatestSourceMarkInserted(): Unit = {
    import concurrent.duration._

    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)

    val Right(sourceId) = helper.writable insert SourceParts(
      title = "sample title",
      url = "http://example.com/1",
      description = "sample description",
      createdAt = Date.current()
    )
    val updatedSource = DummyEntryBinder(helper).bind(sourceId, Seq(
      LoadedEntry(
        title = "sample entry1",
        content = "sample content1",
        author = "sample author1",
        url = EntryUrl("http://example.com/entry/1"),
        createdAt = Date.current() + 1.day
      ),
      LoadedEntry(
        title = "sample entry3",
        content = "sample content3",
        author = "sample author3",
        url = EntryUrl("http://example.com/entry/3"),
        createdAt = Date.current() + 3.day
      ),
      LoadedEntry(
        title = "sample entry2",
        content = "sample content2",
        author = "sample author2",
        url = EntryUrl("http://example.com/entry/2"),
        createdAt = Date.current() + 2.day
      )
    ))
    val OptionRight(Some(mark)) = helper.selectorOf[retrieved_source_marks] findBy sourceId
    assertEquals(
      updatedSource.source.validEntries.head.createdAt.format,
      mark.latest_entry_created_at.typed.format
    )
  }
}
