package x7c1.linen;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import x7c1.linen.glue.activity.ActivityControl;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;

import static android.view.KeyEvent.KEYCODE_BACK;

public class BaseActivity extends Activity implements ActivityControl, ServiceControl {

	@Override
	public void startActivityBy(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.animator.slide_in, R.animator.slide_out);
	}

	@Override
	public Class<?> getClassOf(ActivityLabel label) {
		return Control.getActivityClassOf(label);
	}

	@Override
	public Class<?> getClassOf(ServiceLabel label) {
		return Control.getServiceClassOf(label);
	}

	protected boolean onKeyDownFromChild(int keyCode, KeyEvent event){
		final boolean consumed;
		if (keyCode == KEYCODE_BACK){
			finish();
			overridePendingTransition(R.animator.back_slide_in, R.animator.back_slide_out);
			consumed = true;
		} else {
			consumed = super.onKeyDown(keyCode, event);
		}
		return consumed;
	}
	protected void finishFromChild(){
		super.finish();
		overridePendingTransition(R.animator.back_slide_in, R.animator.back_slide_out);
	}
}
