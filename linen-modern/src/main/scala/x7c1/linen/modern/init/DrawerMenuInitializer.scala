package x7c1.linen.modern.init

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{MenuRowLabel, MenuRowItem, MainLayout}
import x7c1.linen.modern.display.DrawerMenuRowAdapter
import x7c1.wheat.ancient.resource.ViewHolderProvider

trait DrawerMenuInitializer {

  def layout: MainLayout
  def menuLabelProvider: ViewHolderProvider[MenuRowLabel]
  def menuItemProvider: ViewHolderProvider[MenuRowItem]

  def setupDrawerMenu(): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuList setAdapter new DrawerMenuRowAdapter(
      menuLabelProvider,
      menuItemProvider
    )
  }
}
