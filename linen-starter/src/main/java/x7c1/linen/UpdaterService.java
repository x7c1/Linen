package x7c1.linen;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import x7c1.linen.base.Control;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;
import x7c1.linen.modern.init.updater.UpdaterServiceDelegatee;

public class UpdaterService extends Service implements ServiceControl {
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
		return this.delegatee.onStartCommand(intent, flags, startId).value();
	}

	@Override
	public void onDestroy() {
		this.delegatee.onDestroy();
	}

	@Override
	public Class<?> getClassOf(ServiceLabel label) {
		return Control.getServiceClassOf(label);
	}
}
