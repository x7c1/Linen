package x7c1.linen.settings;

import android.os.Bundle;

import x7c1.linen.BaseActivity;
import x7c1.linen.R;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingMyChannelsLayout;
import x7c1.linen.modern.init.settings.my.MyChannelsDelegatee;
import x7c1.linen.res.layout.SettingMyChannelsLayoutProvider;
import x7c1.linen.res.layout.SettingMyChannelRowProvider;

public class MyChannelsActivity extends BaseActivity {
	private MyChannelsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingMyChannelsLayout layout =
				new SettingMyChannelsLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new MyChannelsDelegatee(
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
