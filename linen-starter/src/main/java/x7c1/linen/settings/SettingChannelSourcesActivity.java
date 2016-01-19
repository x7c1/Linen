package x7c1.linen.settings;

import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.BaseActivity;
import x7c1.linen.modern.init.settings.ChannelSourcesDelegatee;

public class SettingChannelSourcesActivity extends BaseActivity {

	private ChannelSourcesDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		delegatee = new ChannelSourcesDelegatee(this);
		delegatee.setup();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		return onKeyDownFromChild(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.close();
	}
	@Override
	public void finish() {
		finishFromChild();
	}
}
