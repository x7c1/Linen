package x7c1.linen;

import android.view.KeyEvent;

import static android.view.KeyEvent.KEYCODE_BACK;

public class SettingChannelsActivity extends BaseActivity {

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
