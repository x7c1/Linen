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
	public void startActivity(Intent intent) {
		super.startActivity(intent);
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
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		transitAnimation.finish();
	}
	@Override
	public void finish(){
		super.finish();
		transitAnimation.finish();
	}
}
