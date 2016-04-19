package x7c1.wheat.modern.action

import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.{Context, Intent}

class ActivityIntentFactory private (
  context: Context,
  action: String,
  intentType: String ){

  private val manager = context.getPackageManager

  def createFor(packageName: String): Either[ActivityIntentError, Intent] = for {
    _ <- confirmPackageInfo(packageName).right
    intent <- createIntent(packageName).right
  } yield {
    intent
  }
  private def confirmPackageInfo(packageName: String) = {
    try {
      val info = manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      Right apply info
    } catch {
      case e: NameNotFoundException =>
        Left apply PackageNotInstalled(packageName)
    }
  }
  private def createIntent(packageName: String) = {
    val intent = new Intent()
    intent setAction action
    intent setType intentType

    import collection.JavaConverters._
    manager.queryIntentActivities(intent, 0).asScala collectFirst {
      case info if info.activityInfo.packageName == packageName =>
        intent.setClassName(
          info.activityInfo.packageName,
          info.activityInfo.name
        )
    } match {
      case Some(x) => Right(x)
      case None => Left(ActivityNotFound(packageName))
    }
  }
}

object ActivityIntentFactory {
  def apply(context: Context): ActivityIntentFactory = {
    new ActivityIntentFactory(
      context = context,
      action = Intent.ACTION_SEND,
      intentType = "text/plain"
    )
  }
}

sealed trait ActivityIntentError

case class PackageNotInstalled(name: String) extends ActivityIntentError

case class ActivityNotFound(name: String) extends ActivityIntentError
