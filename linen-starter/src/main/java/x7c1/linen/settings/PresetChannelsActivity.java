package x7c1.linen.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import x7c1.linen.R;
import x7c1.linen.base.Control;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.activity.ActivityControl;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.res.layout.SettingPresetChannelsLayout;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;
import x7c1.linen.modern.init.settings.preset.PresetChannelsDelegatee;
import x7c1.linen.modern.init.settings.preset.ProviderFactories;
import x7c1.linen.res.layout.SettingPresetChannelsLayoutProvider;
import x7c1.linen.res.layout.SettingPresetChannelRowProvider;
import x7c1.linen.res.layout.SettingPresetTabAllProvider;
import x7c1.linen.res.layout.SettingPresetTabSelectedProvider;

public class PresetChannelsActivity
		extends FragmentActivity implements ActivityControl, ServiceControl {

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
	public void onBackPressed() {
		super.onBackPressed();
		TransitAnimations.forDirectChild(this).finish();
	}

	@Override
	public void finish() {
		super.finish();
		TransitAnimations.forDirectChild(this).finish();
	}

	@Override
	public void startActivityBy(Intent intent) {
		startActivity(intent);
		TransitAnimations.forDirectChild(this).start();
	}

	@Override
	public Class<?> getClassOf(ActivityLabel label) {
		return Control.getActivityClassOf(label);
	}

	@Override
	public Class<?> getClassOf(ServiceLabel label) {
		return Control.getServiceClassOf(label);
	}
}
