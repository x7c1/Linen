package x7c1.linen.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import x7c1.linen.R;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingPresetChannelsLayout;
import x7c1.linen.modern.init.settings.preset.PresetChannelsDelegatee;
import x7c1.linen.modern.init.settings.preset.ProviderFactories;
import x7c1.linen.res.layout.SettingPresetChannelsLayoutProvider;
import x7c1.linen.res.layout.SettingPresetRowProvider;
import x7c1.linen.res.layout.SettingPresetTabAllProvider;
import x7c1.linen.res.layout.SettingPresetTabSelectedProvider;

public class PresetChannelsActivity extends FragmentActivity {
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
						SettingPresetRowProvider.factory()
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
	public void onBackPressed() {
		super.onBackPressed();
		TransitAnimations.forDirectChild(this).finish();
	}

	@Override
	public void finish() {
		super.finish();
		TransitAnimations.forDirectChild(this).finish();
	}
}
