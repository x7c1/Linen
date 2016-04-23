package x7c1.linen.scene.channel.menu

import android.content.Context
import x7c1.linen.repository.channel.ChannelIdentifiable
import x7c1.wheat.modern.menu.popup.{PopupMenuBox, PopupMenuItem}

trait OnMenuSelectedListener[A] {
  def onMenuSelected(e: MenuSelected[A]): Unit
}

object OnMenuSelectedListener {
  def create[A: ChannelIdentifiable](context: Context)
    (f: MenuSelected[A] => Seq[PopupMenuItem]): OnMenuSelectedListener[A] = {

    new OnMenuSelectedListener[A]{
      override def onMenuSelected(e: MenuSelected[A]): Unit = {
        val items = f(e)
        PopupMenuBox(context, e.targetView, items).show()
      }
    }
  }
}
