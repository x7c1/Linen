package x7c1.linen.database.struct

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.robolectric.annotation.Config
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.InspectorLoadingStatus.Loading
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.testing.{AllowTraversingAll, LogSetting}

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class InspectorActionRecordTest extends JUnitSuiteLike with LogSetting with AllowTraversingAll {
  @Test
  def testInsertAndSelect() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val account = factory.createAccount()

    val Right(id1) = helper.writable insert InspectorActionParts(
      loadingStatus = Loading,
      accountId = account.accountId,
      originTitle = "sample article1",
      originUrl = "http://example.com/feed1.xml",
      createdAt = Date.current(),
      updatedAt = Date.current()
    )
    val Right(id2) = helper.writable insert InspectorActionParts(
      loadingStatus = Loading,
      accountId = account.accountId,
      originTitle = "sample article2",
      originUrl = "http://example.com/feed2.xml",
      createdAt = Date.current(),
      updatedAt = Date.current()
    )
    val Right(sequence) = helper.selectorOf[InspectorActionRecord].traverseAll()
    assertEquals(2, sequence.length)

    {
      val Some(record) = sequence.toSeq.find(_.action_id == id1)
      assertEquals("sample article1", record.origin_title)
      assertEquals("http://example.com/feed1.xml", record.origin_url)
      assertEquals(Loading, record.action_loading_status.typed)
    }

    {
      val Some(record) = sequence.toSeq.find(_.action_id == id2)
      assertEquals("sample article2", record.origin_title)
      assertEquals("http://example.com/feed2.xml", record.origin_url)
      assertEquals(Loading, record.action_loading_status.typed)
    }

  }
}
