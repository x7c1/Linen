package x7c1.linen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import x7c1.linen.modern.init.updater.UpdaterServiceDelegatee;

public class UpdaterService extends Service {
	private UpdaterServiceDelegatee delegatee = null;

	@Override
	public void onCreate() {
		this.delegatee = new UpdaterServiceDelegatee(this);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return this.delegatee.onBind(intent).getOrElse(null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent notificationIntent = new Intent(this, UpdaterService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent,  0);
		Notification.Builder builder = new Notification.Builder(getApplicationContext());
		builder.setContentIntent(pendingIntent).
				setTicker("processing").// not shown on Lollipop or above
				setContentTitle("title--").
				setContentText("test--").
				setSmallIcon(android.R.drawable.ic_dialog_info);

		Notification notification = builder.build();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(R.string.action_settings, notification);
		startForeground(R.string.action_settings, notification);

		return this.delegatee.onStartCommand(intent, flags, startId).value();
	}

	@Override
	public void onDestroy() {
		this.delegatee.onDestroy();
	}
}
