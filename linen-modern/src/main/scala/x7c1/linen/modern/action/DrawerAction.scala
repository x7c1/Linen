package x7c1.linen.modern.action

import android.support.v4.view.GravityCompat.START
import x7c1.linen.glue.res.layout.MainLayout

class DrawerAction (layout: MainLayout){
  def onBack(): Boolean = {
    (layout.drawerMenu isDrawerOpen START) && close()
  }
  private def close() = {
    layout.drawerMenu closeDrawer START
    true
  }
}
