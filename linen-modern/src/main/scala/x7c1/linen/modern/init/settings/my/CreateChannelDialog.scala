package x7c1.linen.modern.init.settings.my

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.{AlertDialog, AppCompatDialogFragment}
import android.widget.Button
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.res.layout.SettingMyChannelCreate
import x7c1.linen.modern.init.settings.my.CreateChannelDialog.Arguments
import x7c1.linen.repository.channel.my.ChannelCreator.InputToCreate
import x7c1.linen.repository.channel.my.{ChannelCreator, ChannelWriterError, EmptyName, UserInputError}
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.dialog.DelayedDialog
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.dialog.tasks.KeyboardControl


object CreateChannelDialog {

  class Arguments(
    val clientAccountId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val inputLayoutFactory: ViewHolderProviderFactory[SettingMyChannelCreate]
  )

}

class CreateChannelDialog extends AppCompatDialogFragment
  with DelayedDialog
  with TypedFragment[Arguments] {

  lazy val args = getTypedArguments

  private val provide = EitherTask.hold[ChannelWriterError]

  private lazy val helper = new DatabaseHelper(getActivity)

  private lazy val keyboard = {
    KeyboardControl[ChannelWriterError](this, layout.channelName)
  }

  def showIn(activity: FragmentActivity) = {
    show(activity.getSupportFragmentManager, "channel-dialog")
  }

  override def onCreateDialog(savedInstanceState: Bundle) = {
    args.dialogFactory.createAlertDialog(
      title = "Create my channel",
      positiveText = "Create",
      negativeText = "Cancel",
      layoutView = layout.itemView
    )
  }

  override def onStart(): Unit = {
    super.onStart()

    initializeButtons(
      positive = onClickPositive,
      negative = onClickNegative
    )
  }

  override def onStop(): Unit = {
    super.onStop()
    helper.close()
  }

  private def onClickPositive(button: Button) = {
    val tasks = for {
      input <- validateInput
      channelId <- createChannel(input)
      _ <- keyboard.taskToHide()
      _ <- notifyCreated(channelId)
    } yield {
      input
    }
    tasks run {
      case Right(input) =>
        Log info s"channel created: $input"
      case Left(error: UserInputError) =>
        showError(error).execute()
      case Left(error) =>
        Log error error.dump
    }
  }

  private def onClickNegative(button: Button) = {
    Log info s"[init]"
    keyboard.taskToHide().execute()
  }

  private def validateInput = provide {
    def parseName = {
      val name = layout.channelName.text.toString
      if (name.isEmpty) {
        Left(EmptyName())
      } else {
        Right(name)
      }
    }
    def parseDescription = {
      val description = layout.channelDescription.text.toString
      Right(Option(description))
    }
    for {
      name <- parseName.right
      description <- parseDescription.right
    } yield InputToCreate(
      channelName = name,
      description = description
    )
  }

  private def createChannel(input: InputToCreate) = provide async {
    val factory = ChannelCreator(helper, args.clientAccountId)
    factory createChannel input
  }

  private def showError(error: UserInputError) = provide ui {
    error match {
      case EmptyName() => layout.channelNameLayout setError error.message
    }
  }

  private def notifyCreated(channelId: Long) = provide {
    val event = new ChannelCreated(
      accountId = args.clientAccountId,
      channelId = channelId
    )
    LocalBroadcaster(event) dispatchFrom getActivity
    Right(event)
  }

  private lazy val layout = {
    val factory = args.inputLayoutFactory create getActivity
    factory.inflateOn(null)
  }

}

class ChannelCreated(
  val accountId: Long,
  val channelId: Long
)

object ChannelCreated {

  implicit object account extends HasAccountId[ChannelCreated] {
    override def toId = _.accountId
  }

}
