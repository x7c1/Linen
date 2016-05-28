package x7c1.wheat.modern.decorator

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE

class RichContext[A <: Context](context: A){
  def notificationManager: NotificationManager = {
    context.getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
  }
}
