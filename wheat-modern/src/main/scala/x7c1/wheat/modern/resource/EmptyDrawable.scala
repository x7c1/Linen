package x7c1.wheat.modern.resource

import android.graphics.{Rect, ColorFilter, Canvas}
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter

class EmptyDrawable extends Drawable {
  override def draw(canvas: Canvas): Unit = {}
  override def setColorFilter(colorFilter: ColorFilter): Unit = {}
  override def setAlpha(alpha: Int): Unit = {}
  override def getOpacity: Int = 0
}

object EmptyDrawable {
  def apply(): EmptyDrawable = {
    val drawable = new EmptyDrawable
    drawable setBounds new Rect()
    drawable
  }
}

object EmptyDrawableGetter extends ImageGetter {
  override def getDrawable(source: String): Drawable = EmptyDrawable()
}
