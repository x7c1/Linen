package x7c1.linen.settings;

import android.os.Bundle;

import x7c1.linen.R;
import x7c1.linen.base.BaseActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingChannelSourcesLayout;
import x7c1.linen.modern.init.settings.preset.PresetChannelSourcesDelegatee;
import x7c1.linen.res.layout.SettingChannelSourcesLayoutProvider;
import x7c1.linen.res.layout.SettingChannelSourcesRowProvider;

public class PresetChannelSourcesActivity extends BaseActivity {
	private PresetChannelSourcesDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SettingChannelSourcesLayout layout =
				new SettingChannelSourcesLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		delegatee = new PresetChannelSourcesDelegatee(
				this,
				layout,
				new SettingChannelSourcesRowProvider(this)
		);
		delegatee.setup();
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
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDescendant(this);
	}

	@Override
	public void finish() {
		finishFromChild();
	}
}
