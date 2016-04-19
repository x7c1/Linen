package x7c1.wheat.modern.action

import android.content.{Context, Intent}
import x7c1.wheat.macros.logger.Log

class PlainTextSender (context: Context, packageName: String){

  def share(text: String): Unit = {
    ActivityIntentFactory(context) createFor packageName match {
      case Right(intent) =>
        Log info s"${intent.getComponent}"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context startActivity intent
      case Left(error) =>
        Log error s"$error"
    }
  }
}

object PlainTextSender {
  def apply(context: Context, packageName: String): PlainTextSender = {
    new PlainTextSender(context, packageName)
  }
}

class PocketSender (context: Context){
  def save[A: SiteVisitable](target: A): Unit = {
    val uri = implicitly[SiteVisitable[A]].targetUri(target)
    val sender = PlainTextSender(context, "com.ideashower.readitlater.pro")
    sender share uri.toString
  }
}

object PocketSender {
  def apply(context: Context): PocketSender = {
    new PocketSender(context)
  }
}
