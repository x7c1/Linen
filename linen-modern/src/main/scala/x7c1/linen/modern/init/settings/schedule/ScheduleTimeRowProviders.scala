package x7c1.linen.modern.init.settings.schedule

import x7c1.linen.glue.res.layout.{SettingScheduleTimeRowItem, SettingScheduleTimeRow}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object ScheduleTimeRowProviders
  extends WithSingleProvider[ScheduleTimeRowProviders](_.forItem)

class ScheduleTimeRowProviders(
  val forItem: ViewHolderProvider[SettingScheduleTimeRowItem]
) extends ViewHolderProviders[SettingScheduleTimeRow]{
  override protected def all = Seq(forItem)
}
