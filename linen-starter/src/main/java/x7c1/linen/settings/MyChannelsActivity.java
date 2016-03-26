package x7c1.linen.settings;

import android.os.Bundle;

import x7c1.linen.BaseActivity;
import x7c1.linen.R;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingChannelsLayout;
import x7c1.linen.modern.init.settings.SettingChannelsDelegatee;
import x7c1.linen.res.layout.SettingChannelsLayoutProvider;
import x7c1.linen.res.layout.SettingMyChannelRowProvider;

public class MyChannelsActivity extends BaseActivity {
	private SettingChannelsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingChannelsLayout layout =
				new SettingChannelsLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new SettingChannelsDelegatee(
				this,
				layout,
				new SettingMyChannelRowProvider(this)
		);
		this.delegatee.setup();
	}
	@Override
	public void onBackPressed() {
		onBackPressedAtChild();
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

	@Override
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDirectChild(this);
	}
}
