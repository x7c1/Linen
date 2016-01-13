package x7c1.linen;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import x7c1.linen.modern.init.updater.UpdaterDelegatee;

public class UpdaterService extends Service {
	private UpdaterDelegatee delegatee = null;

	@Override
	public void onCreate() {
		this.delegatee = new UpdaterDelegatee(this);
		this.delegatee.create();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return this.delegatee.setupBinder(intent).getOrElse(null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return this.delegatee.startCommand(intent, flags, startId).value();
	}

	@Override
	public void onDestroy() {
		this.delegatee.destroy();
	}
}
