package x7c1.linen.settings;

import android.os.Bundle;

import x7c1.linen.R;
import x7c1.linen.base.BaseFragmentActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingPresetChannelsLayout;
import x7c1.linen.modern.init.settings.preset.PresetChannelsDelegatee;
import x7c1.linen.modern.init.settings.preset.ProviderFactories;
import x7c1.linen.res.layout.SettingPresetChannelRowProvider;
import x7c1.linen.res.layout.SettingPresetChannelsLayoutProvider;
import x7c1.linen.res.layout.SettingPresetTabAllProvider;
import x7c1.linen.res.layout.SettingPresetTabSelectedProvider;

public class PresetChannelsActivity extends BaseFragmentActivity {

	private PresetChannelsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingPresetChannelsLayout layout =
				new SettingPresetChannelsLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new PresetChannelsDelegatee(
				this,
				layout,
				new ProviderFactories(
						SettingPresetTabSelectedProvider.factory(),
						SettingPresetTabAllProvider.factory(),
						SettingPresetChannelRowProvider.factory()
				)
		);
		this.delegatee.onCreate();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.onDestroy();
	}
	@Override
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDirectChild(this);
	}
}
