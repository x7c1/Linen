package x7c1.linen.modern.init.dev

import android.app.Activity
import android.widget.Toast
import x7c1.linen.glue.res.layout.DevCreateDummiesLayout
import x7c1.linen.modern.accessor.LinenDatabase
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync.async
import x7c1.wheat.modern.decorator.Imports._


class CreateDummiesDelegatee (
  activity: Activity,
  layout: DevCreateDummiesLayout){

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.createDummies onClick { _ =>
      async {
        DummyFactory.createDummies(activity)(100)
        layout.createDummies runUi { _ =>
          Toast.makeText(activity, "dummies inserted", Toast.LENGTH_SHORT).show()
        }
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
