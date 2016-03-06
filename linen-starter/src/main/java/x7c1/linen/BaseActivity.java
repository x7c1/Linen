package x7c1.linen;

import android.app.Activity;
import android.content.Intent;

import x7c1.linen.glue.activity.ActivityControl;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;

public class BaseActivity extends Activity implements ActivityControl, ServiceControl {

	@Override
	public void startActivityBy(Intent intent) {
		startActivity(intent);
		new ChildActivityDelegatee(this).start();
	}

	@Override
	public Class<?> getClassOf(ActivityLabel label) {
		return Control.getActivityClassOf(label);
	}

	@Override
	public Class<?> getClassOf(ServiceLabel label) {
		return Control.getServiceClassOf(label);
	}

	protected void onBackPressedAtChild() {
		super.onBackPressed();
		new ChildActivityDelegatee(this).finish();
	}

	protected void finishFromChild(){
		super.finish();
		new ChildActivityDelegatee(this).finish();
	}

}
