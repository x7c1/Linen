package x7c1.linen.database.struct

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.InspectorLoadingStatus.Loading
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.repository.source.unread.UnreadSourceFixture
import x7c1.linen.testing.{AllowTraversingAll, LogSetting}

@Config(manifest = Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class InspectorSourceRecordTest extends JUnitSuiteLike with LogSetting with AllowTraversingAll {

  @Test
  def testInsertAndSelect() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val fixture = new UnreadSourceFixture(helper)

    val Right(actionId) = helper.writable insert InspectorActionParts(
      loadingStatus = Loading,
      accountId = fixture.account1.accountId,
      originTitle = "sample article1",
      originUrl = "http://example.com/entry1.php",
      createdAt = Date.current(),
      updatedAt = Date.current()
    )
    val Right(id1) = helper.writable insert InspectorSourceParts(
      actionId = actionId,
      loadingStatus = Loading,
      latentUrl = "http://example.com/feed1.xml",
      discoveredSourceId = Some(fixture.sourceId1),
      createdAt = Date.current(),
      updatedAt = Date.current()
    )
    val Right(id2) = helper.writable insert InspectorSourceParts(
      actionId = actionId,
      loadingStatus = Loading,
      latentUrl = "http://example.com/feed2.xml",
      discoveredSourceId = Some(fixture.sourceId2),
      createdAt = Date.current(),
      updatedAt = Date.current()
    )
    val Right(sequence) = helper.selectorOf[InspectorSourceRecord].traverseAll()
    assertEquals(sequence.length, 2)

    val Some(record1) = sequence.toSeq.find(_.discovered_source_id contains fixture.sourceId1)
    assertEquals(record1.latent_source_url, "http://example.com/feed1.xml")

    val Some(record2) = sequence.toSeq.find(_.discovered_source_id contains fixture.sourceId2)
    assertEquals(record2.latent_source_url, "http://example.com/feed2.xml")
  }
}
