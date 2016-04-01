package x7c1.linen.modern.init.settings.my

import android.app.{Activity, Dialog}
import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.{AlertDialog, AppCompatDialogFragment}
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingMyChannelSources
import x7c1.linen.glue.res.layout.{SettingMyChannelCreate, SettingMyChannelRow, SettingMyChannelsLayout}
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.setting.MyChannelAccessor
import x7c1.linen.modern.accessor.{AccountIdentifiable, LinenOpenHelper}
import x7c1.linen.modern.display.settings.{ChannelRowAdapter, ChannelSourcesSelected, MyChannelSubscriptionChanged}
import x7c1.linen.modern.init.settings.my.CreateChannelDialog.Arguments
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.macros.fragment.{FragmentFactory, TypedFragment}
import x7c1.wheat.macros.intent.{IntentExpander, IntentFactory, LocalBroadcaster}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class MyChannelsDelegatee (
  activity: FragmentActivity with ActivityControl,
  layout: SettingMyChannelsLayout,
  dialogArguments: Arguments,
  channelRowProvider: ViewHolderProvider[SettingMyChannelRow] ){

  private lazy val helper = new LinenOpenHelper(activity)

  private lazy val database = helper.getReadableDatabase

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.channelList setLayoutManager manager

    IntentExpander executeBy activity.getIntent
  }
  def showMyChannels(accountId: Long) = {
    layout.channelList setAdapter new ChannelRowAdapter(
      accessor = MyChannelAccessor.create(database, accountId),
      viewHolderProvider = channelRowProvider,
      onSourcesSelected = new OnChannelSourcesSelected(activity).onSourcesSelected,
      onSubscriptionChanged = {
        val listener = new OnMyChannelSubscriptionChanged(
          context = activity,
          helper = helper,
          account = AccountIdentifiable(accountId)
        )
        listener.updateSubscription
      }
    )
    layout.buttonToCreate onClick { _ => showInputDialog(accountId) }
  }
  def close(): Unit = {
    database.close()
    helper.close()
    Log info "[done]"
  }
  private def showInputDialog(accountId: Long): Unit = {
    Log info s"[init] account:$accountId"

    val fragment = FragmentFactory.create[CreateChannelDialog] by dialogArguments
    fragment.show(activity.getSupportFragmentManager, "hoge")
  }
}

object CreateChannelDialog {
  class Arguments(
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val inputLayoutFactory: ViewHolderProviderFactory[SettingMyChannelCreate]
  )
}

class CreateChannelDialog extends AppCompatDialogFragment with TypedFragment[Arguments]{
  lazy val args = getTypedArguments

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val builder =
      args.dialogFactory.newInstance(getActivity).
      setTitle("Create new channel").
      setPositiveButton("Create", new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = {
          Log info s"[init] create - $which"
        }
      }).
      setNegativeButton("Cancel", new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = {
          Log info s"[init] cancel - $which"
        }
      })

    val converter = MetricsConverter(getActivity)
    val left = converter dipToPixel 24
    val top = converter dipToPixel 12
    val right = converter dipToPixel 24
    val bottom = converter dipToPixel 0
    val factory = args.inputLayoutFactory create getActivity
    val layout = factory.inflateOn(null)

    val view = layout.itemView
    builder.setView(view, left, top, right, bottom)
    builder.create()
  }

  override def setupDialog(dialog: Dialog, style: Int): Unit = super.setupDialog(dialog, style)
}

class OnChannelSourcesSelected(activity: Activity with ActivityControl){
  def onSourcesSelected(event: ChannelSourcesSelected): Unit = {
    Log info s"[init] $event"

    val intent = IntentFactory.using[MyChannelSourcesDelegatee].
      create(activity, activity getClassOf SettingMyChannelSources){
        _.showSources(event)
      }

    activity startActivityBy intent
  }
}

class OnMyChannelSubscriptionChanged(
  context: Context,
  helper: LinenOpenHelper,
  account: AccountIdentifiable){

  def updateSubscription(event: MyChannelSubscriptionChanged): Unit = {
    val subscriber = new ChannelSubscriber(account, helper)
    if (event.isSubscribed){
      subscriber subscribe event.channelId
    } else {
      subscriber unsubscribe event.channelId
    }
    LocalBroadcaster(event) dispatchFrom context
  }
}
