package x7c1.linen.scene.channel.menu

import android.content.Context
import x7c1.wheat.modern.menu.popup.{PopupMenuBox, PopupMenuItem}

trait OnMenuSelectedListener {
  def onMenuSelected(e: MenuSelected): Unit
}

object OnMenuSelectedListener {
  def create(context: Context)
    (f: MenuSelected => Seq[PopupMenuItem]): OnMenuSelectedListener = {

    new OnMenuSelectedListener {
      override def onMenuSelected(e: MenuSelected): Unit = {
        val items = f(e)
        PopupMenuBox(context, e.targetView, items).show()
      }
    }
  }
}
