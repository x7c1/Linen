package x7c1.linen;

import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.glue.res.layout.SettingChannelsLayout;
import x7c1.linen.modern.init.settings.SettingChannelsDelegatee;
import x7c1.linen.res.layout.SettingChannelsLayoutProvider;
import x7c1.linen.res.layout.SettingChannelsRowProvider;

import static android.view.KeyEvent.KEYCODE_BACK;

public class SettingChannelsActivity extends BaseActivity {
	private SettingChannelsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingChannelsLayout layout =
				new SettingChannelsLayoutProvider(this).inflate(null, false);

		setContentView(layout.itemView);

		this.delegatee = new SettingChannelsDelegatee(
				this,
				layout,
				new SettingChannelsRowProvider(this)
		);
		this.delegatee.setup();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.close();
	}
}
