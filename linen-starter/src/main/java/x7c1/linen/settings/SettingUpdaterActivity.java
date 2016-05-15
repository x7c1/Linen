package x7c1.linen.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import x7c1.linen.R;
import x7c1.linen.base.BaseFragmentActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingUpdaterLayout;
import x7c1.linen.modern.init.settings.updater.LoaderScheduleRowProviders;
import x7c1.linen.modern.init.settings.updater.SettingUpdaterDelegatee;
import x7c1.linen.res.layout.SettingScheduleRowItemProvider;
import x7c1.linen.res.layout.SettingUpdaterLayoutProvider;

public class SettingUpdaterActivity extends BaseFragmentActivity {
	private SettingUpdaterDelegatee delegatee = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingUpdaterLayout layout =
				new SettingUpdaterLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new SettingUpdaterDelegatee(
				this,
				layout,
				new LoaderScheduleRowProviders(
						new SettingScheduleRowItemProvider(this)
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
