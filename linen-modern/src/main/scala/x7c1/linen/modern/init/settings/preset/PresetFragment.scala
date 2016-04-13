package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingPresetChannelRow
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.preset.{PresetChannelAccessorFactory, PresetChannelsAccessor}
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.logger.Log

trait PresetFragment { self: Fragment =>

  def reload(channelId: Long): Unit

  def activity: Activity with ActivityControl with ServiceControl =
    getActivity.asInstanceOf[Activity with ActivityControl with ServiceControl]

  protected def args: PresetFragmentArguments

  protected def accessorFactory: PresetChannelAccessorFactory

  protected lazy val helper = new DatabaseHelper(getContext)

  protected lazy val presetsAccessor = {
    accessorFactory.create(args.accountId, helper) match {
      case Right(accessor) => Some(accessor)
      case Left(error) =>
        Log error error.toString
        None
    }
  }
  protected def toAdapter
    (location: PresetEventLocation)(accessor: PresetChannelsAccessor) = {

    val factory = new PresetsChannelsAdapterFactory(
      activity, args.rowFactory, location, helper, args.accountId
    )
    factory.createAdapter(accessor)
  }
  protected def applyAdapterTo(channelList: RecyclerView, from: PresetEventLocation) = {
    presetsAccessor map toAdapter(from) foreach { adapter =>
      channelList setLayoutManager new LinearLayoutManager(getContext)
      channelList setAdapter adapter
    }
  }
}

trait PresetFragmentArguments{
  def accountId: Long
  def rowFactory: ViewHolderProviderFactory[SettingPresetChannelRow]
}
