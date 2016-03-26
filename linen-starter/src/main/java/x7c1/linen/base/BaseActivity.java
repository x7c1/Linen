package x7c1.linen.base;

import android.app.Activity;
import android.content.Intent;

import x7c1.linen.glue.activity.ActivityControl;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;

abstract public class BaseActivity extends Activity implements ActivityControl, ServiceControl {

	protected TransitAnimation transitAnimation = createTransitAnimation();
	protected abstract TransitAnimation createTransitAnimation();

	@Override
	public void startActivityBy(Intent intent) {
		startActivity(intent);
		transitAnimation.start();
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
		transitAnimation.finish();
	}
	protected void finishFromChild(){
		super.finish();
		transitAnimation.finish();
	}
}
