package x7c1.linen.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import x7c1.linen.ChildActivityDelegatee;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingPresetChannelsLayout;
import x7c1.linen.modern.init.settings.PresetChannelsDelegatee;
import x7c1.linen.res.layout.SettingPresetChannelsLayoutProvider;

public class PresetChannelsActivity extends FragmentActivity {
	private PresetChannelsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingPresetChannelsLayout layout =
				new SettingPresetChannelsLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new PresetChannelsDelegatee(this, layout);
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
		new ChildActivityDelegatee(this).finish();
	}

	@Override
	public void finish() {
		super.finish();
		new ChildActivityDelegatee(this).finish();
	}
}