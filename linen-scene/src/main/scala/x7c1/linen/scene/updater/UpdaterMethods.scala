package x7c1.linen.scene.updater

import android.app.Service
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.DummyFactory
import x7c1.linen.repository.preset.PresetFactory
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync.async

class UpdaterMethods(
  service: Service with ServiceControl,
  helper: DatabaseHelper,
  startId: Int){

  def createDummies(max: Int): Unit = async {
    Log info "[init]"
    val notifier = new UpdaterServiceNotifier(service, max, Date.current(), startId)
    DummyFactory.createDummies0(service)(max){ n =>
      notifier.notifyProgress(n)
    }
    notifier.notifyDone()
    service stopSelf startId
  }
  def createPresetJp(): Unit = async {
    Log info "[init]"
    new PresetFactory(helper).setupJapanesePresets()
  }
  def createDummySources(channelIds: Seq[Long]) = async {
    Log info s"$channelIds"
  }
}
