package x7c1.linen;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import x7c1.linen.dev.CreateRecordsActivity;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.activity.ActivityStarter;

import static android.view.KeyEvent.KEYCODE_BACK;
import static java.lang.String.format;

public class BaseActivity extends Activity implements ActivityStarter {

	@Override
	public void transitTo(ActivityLabel label) {
		Intent intent = getIntentOf(label);
		startActivity(intent);
		overridePendingTransition(R.animator.slide_in, R.animator.slide_out);
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
	private Intent getIntentOf(ActivityLabel label) {
		final Intent intent;
		switch (label){
			case SettingChannels:
				intent = new Intent(this, SettingChannelsActivity.class);
				break;
			case CreateRecords:
				intent = new Intent(this, CreateRecordsActivity.class);
				break;
			default:
				throw new IllegalArgumentException(format("unknown label: %s", label));
		}
		return intent;
	}
}
