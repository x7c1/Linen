package x7c1.linen.modern.init.dev

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.LinenDatabase
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.TaskAsync.async


class CreateRecordsDelegatee (
  activity: Activity with ServiceControl,
  layout: DevCreateRecordsLayout){

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.createDummies onClick { _ =>
      async {
        activity startService new Intent(
          activity, activity getClassOf Updater )
      }
    }
    layout.deleteDatabase onClick { _ =>
      activity deleteDatabase LinenDatabase.name
      Toast.makeText(activity, "database deleted", Toast.LENGTH_SHORT).show()
    }
  }

  def close(): Unit = {
    Log info "[done]"
  }
}
