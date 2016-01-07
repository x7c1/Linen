package x7c1.linen;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.activity.ActivityStarter;

public class BaseActivity extends Activity implements ActivityStarter {

	@Override
	public void transitTo(ActivityLabel label) {
		switch (label){
			case SettingChannels:
				Intent intent = new Intent(this, SettingChannelsActivity.class);
				startActivity(intent);
				overridePendingTransition(R.animator.slide_in, R.animator.slide_out);
				break;
		}
	}
}
