package x7c1.linen.database

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{retrieved_source_marks, EntryParts}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.source.unread.UnreadSourceFixture

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class EntryPartsTest extends JUnitSuiteLike {

  @Test
  def testUniqueConstraint() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val fixture = new UnreadSourceFixture(helper)
    val writable = helper.writable
    val sourceId = fixture.sourceId1
    val url = EntryUrl("http://example.com/entry-111")

    val x1 = writable insert EntryParts(
      sourceId = sourceId,
      title = "sample-title",
      content = "sample-content",
      author = "sample-author",
      url = url,
      createdAt = Date.current()
    )
//    x1.left.foreach(println)
    assertEquals(true, x1.isRight)

    val Right(Some(mark1)) = helper.readable.find[retrieved_source_marks].by(sourceId).toEither
    val Right(newEntryId) = x1
    assertEquals(newEntryId, mark1.latest_entry_id)
    assertEquals(sourceId, mark1.source_id)

    val x2 = writable insert EntryParts(
      sourceId = sourceId,
      title = "sample-title-2",
      content = "sample-content-2",
      author = "sample-author-2",
      url = url,
      createdAt = Date.current()
    )
//    x2.left.foreach(println)
    assertEquals(false, x2.isRight)

    val Right(Some(mark2)) = helper.readable.find[retrieved_source_marks].by(sourceId).toEither
    assertEquals(newEntryId, mark2.latest_entry_id)
  }
}
