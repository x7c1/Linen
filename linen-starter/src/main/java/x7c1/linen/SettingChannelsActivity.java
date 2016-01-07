package x7c1.linen;

import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.glue.res.layout.SettingChannelsLayout;
import x7c1.linen.res.layout.SettingChannelsLayoutProvider;

import static android.view.KeyEvent.KEYCODE_BACK;

public class SettingChannelsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingChannelsLayout layout =
				new SettingChannelsLayoutProvider(this).inflate(null, false);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (keyCode == KEYCODE_BACK){
			finish();
			overridePendingTransition(R.animator.back_slide_in, R.animator.back_slide_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
