package x7c1.linen.modern.init.settings.my

import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.{AlertDialog, AppCompatDialogFragment}
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.ChannelParts
import x7c1.linen.glue.res.layout.SettingMyChannelCreate
import x7c1.linen.modern.init.settings.my.CreateChannelDialog.Arguments
import x7c1.linen.repository.account.AccountIdentifiable
import x7c1.linen.repository.channel.subscribe.ChannelSubscriber
import x7c1.linen.repository.date.Date
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.decorator.Imports._


object CreateChannelDialog {
  class Arguments(
    val clientAccountId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val inputLayoutFactory: ViewHolderProviderFactory[SettingMyChannelCreate]
  )
}

class CreateChannelDialog extends AppCompatDialogFragment with TypedFragment[Arguments]{
  lazy val args = getTypedArguments

  private val provide = EitherTask.hold[NewChannelError]

  private lazy val helper = new DatabaseHelper(getActivity)

  def showIn(activity: FragmentActivity) = {
    show(activity.getSupportFragmentManager, "channel-dialog")
  }
  override def onCreateDialog(savedInstanceState: Bundle) = internalDialog

  override def onStart(): Unit = {
    super.onStart()

    getDialog match {
      case dialog: AlertDialog =>
        dialog.positiveButton foreach (_ onClick onClickPositive)
        dialog.negativeButton foreach (_ onClick onClickNegative)
      case dialog =>
        Log error s"unknown dialog $dialog"
    }
  }

  override def onStop(): Unit = {
    super.onStop()
    helper.close()
  }

  private def onClickPositive(button: Button) = {
    val tasks = for {
      input <- validateInput
      channelId <- createChannel(input)
      _ <- hideKeyboard()
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
    hideKeyboard().execute()
  }
  private def validateInput = provide {
    def parseName = {
      val name = layout.channelName.text.toString
      if (name.isEmpty){
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
    } yield NewChannelInput(
      channelName = name,
      description = description
    )
  }
  private def createChannel(input: NewChannelInput) = provide async {
    Log info s"[create] account:${args.clientAccountId}"

    def create() = helper.writable insert ChannelParts(
      accountId = args.clientAccountId,
      name = input.channelName,
      description = input.description.getOrElse(""),
      createdAt = Date.current()
    )
    def subscribe(channelId: Long) = {
      val subscriber = new ChannelSubscriber(
        account = AccountIdentifiable(args.clientAccountId),
        helper = helper
      )
      subscriber subscribe channelId
    }
    // todo: use transaction
    val either = for {
      channelId <- create().right
      _ <- subscribe(channelId).toEither.right
    } yield {
      channelId
    }
    either.left map { e => SqlError(e) }
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
  private def hideKeyboard() = provide ui {
    Option(getActivity.getCurrentFocus) match {
      case Some(view) =>
        Log info s"[focus] $view"

        val manager = getActivity.
          getSystemService(Context.INPUT_METHOD_SERVICE).
          asInstanceOf[InputMethodManager]

        Log info s"[focus-token] ${view.getWindowToken}"
        manager.hideSoftInputFromWindow(
          layout.channelName.getWindowToken,
          InputMethodManager.HIDE_NOT_ALWAYS
        )
        val shown = manager.isAcceptingText
        Log info s"focus? $shown"

        val msec = if (shown) 300 else 200
        view.runAfter(msec){ _ => dismiss() }

      case None =>
        Log warn s"[unfocused]"
        dismiss()
    }
  }

  private lazy val layout = {
    val factory = args.inputLayoutFactory create getActivity
    factory.inflateOn(null)
  }

  private lazy val internalDialog = {
    val nop = new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"[init]"
      }
    }
    /*
      In order to control timing of dismiss(),
        temporally set listeners as nop
        then set onClickListener again in onStart method.
     */
    val builder = args.dialogFactory.newInstance(getActivity).
      setTitle("Create new channel").
      setPositiveButton("Create", nop).
      setNegativeButton("Cancel", nop)

    builder setView layout.itemView
    builder.create()
  }
}

case class NewChannelInput(
  channelName: String,
  description: Option[String]
)

class ChannelCreated(
  val accountId: Long,
  val channelId: Long
) extends AccountIdentifiable
