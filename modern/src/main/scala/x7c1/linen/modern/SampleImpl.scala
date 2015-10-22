package x7c1.linen.modern

import android.support.v7.app.AppCompatActivity
import x7c1.linen.interfaces.SampleStruct

class SampleImpl extends SampleStruct {
  override def getFoo(activity: AppCompatActivity): String = activity.toString
}
