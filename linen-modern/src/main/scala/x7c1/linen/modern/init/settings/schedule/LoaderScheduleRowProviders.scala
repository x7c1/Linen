package x7c1.linen.modern.init.settings.schedule

import x7c1.linen.glue.res.layout.{SettingScheduleRow, SettingScheduleRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object LoaderScheduleRowProviders
  extends WithSingleProvider[LoaderScheduleRowProviders](_.forItem)

class LoaderScheduleRowProviders (
  val forItem: ViewHolderProvider[SettingScheduleRowItem]
) extends ViewHolderProviders[SettingScheduleRow]{

  override protected def all = Seq(
    forItem
  )
}
