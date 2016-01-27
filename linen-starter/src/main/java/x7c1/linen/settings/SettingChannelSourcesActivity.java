package x7c1.linen.settings;

import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.BaseActivity;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingChannelSourcesLayout;
import x7c1.linen.modern.init.settings.ChannelSourcesDelegatee;
import x7c1.linen.res.layout.SettingChannelSourcesLayoutProvider;
import x7c1.linen.res.layout.SettingChannelSourcesRowProvider;

public class SettingChannelSourcesActivity extends BaseActivity {
	private ChannelSourcesDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingChannelSourcesLayout layout =
				new SettingChannelSourcesLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		delegatee = new ChannelSourcesDelegatee(
				this,
				layout,
				new SettingChannelSourcesRowProvider(this)
		);
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
