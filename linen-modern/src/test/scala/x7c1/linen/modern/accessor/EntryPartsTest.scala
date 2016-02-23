package x7c1.linen.modern.accessor

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.struct.Date

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class EntryPartsTest extends JUnitSuiteLike {

  @Test
  def testUniqueConstraint() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val fixture = new UnreadSourceFixture(helper)
    val writable = helper.writableDatabase
    val sourceId = fixture.sourceId1
    val url = EntryUrl("http://example.com/entry-111")

    val x1 = writable insert EntryParts(
      sourceId = sourceId,
      title = "sample-title",
      content = "sample-content",
      url = url,
      createdAt = Date.current()
    )
//    x1.left.foreach(println)
    assertEquals(true, x1.isRight)

    val Right(Some(mark1)) = helper.readable.find[retrieved_source_marks](sourceId)
    val Right(newEntryId) = x1
    assertEquals(newEntryId, mark1.latest_entry_id)
    assertEquals(sourceId, mark1.source_id)

    val x2 = writable insert EntryParts(
      sourceId = sourceId,
      title = "sample-title-2",
      content = "sample-content-2",
      url = url,
      createdAt = Date.current()
    )
//    x2.left.foreach(println)
    assertEquals(false, x2.isRight)

    val Right(Some(mark2)) = helper.readable.find[retrieved_source_marks](sourceId)
    assertEquals(newEntryId, mark2.latest_entry_id)
  }
}
