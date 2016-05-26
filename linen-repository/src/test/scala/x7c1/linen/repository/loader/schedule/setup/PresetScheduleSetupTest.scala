package x7c1.linen.repository.loader.schedule.setup


import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.loader.schedule.PresetLoaderSchedule
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.testing.LogSetting
import x7c1.wheat.modern.either.OptionRight


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetScheduleSetupTest extends JUnitSuiteLike with LogSetting {

  @Test
  def testSetupFor() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val account1 = factory.createAccount()
    val account2 = factory.createAccount()

    val before1 = helper.selectorOf[PresetLoaderSchedule] findBy account1
    val before2 = helper.selectorOf[PresetLoaderSchedule] findBy account2
    assertEquals(OptionRight(None), before1)
    assertEquals(OptionRight(None), before2)

    PresetScheduleSetup(helper).setupFor(account1)

    val OptionRight(Some(after1)) = helper.selectorOf[PresetLoaderSchedule] findBy account1
    assertEquals(true, after1.isInstanceOf[PresetLoaderSchedule])
    assertEquals(OptionRight(None),
      helper.selectorOf[PresetLoaderSchedule] findBy account2
    )

    PresetScheduleSetup(helper).setupFor(account1)

    val OptionRight(Some(after2)) = helper.selectorOf[PresetLoaderSchedule] findBy account1
    assertEquals(true, after1.scheduleId == after2.scheduleId)
  }
}
