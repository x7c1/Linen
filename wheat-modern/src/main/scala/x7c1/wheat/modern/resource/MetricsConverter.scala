package x7c1.wheat.modern.resource

import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.{DisplayMetrics, TypedValue}


class MetricsConverter private (metrics: => DisplayMetrics){
  def dipToPixel(dip: Int): Int = {
    TypedValue.applyDimension(COMPLEX_UNIT_DIP, dip, metrics).toInt
  }
}

object MetricsConverter {
  def apply(context: Context): MetricsConverter = {
    new MetricsConverter(context.getResources.getDisplayMetrics)
  }
}
