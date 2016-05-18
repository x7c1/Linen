package x7c1.linen.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import x7c1.linen.R;
import x7c1.linen.base.BaseFragmentActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingScheduleLayout;
import x7c1.linen.modern.init.settings.schedule.LoaderScheduleRowProviders;
import x7c1.linen.modern.init.settings.schedule.LoaderSchedulesDelegatee;
import x7c1.linen.modern.init.settings.schedule.ScheduleTimeRowProviders;
import x7c1.linen.res.layout.SettingScheduleLayoutProvider;
import x7c1.linen.res.layout.SettingScheduleRowItemProvider;
import x7c1.linen.res.layout.SettingScheduleTimeRowItemProvider;

public class LoaderSchedulesActivity extends BaseFragmentActivity {
	private LoaderSchedulesDelegatee delegatee = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SettingScheduleLayout layout =
				new SettingScheduleLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new LoaderSchedulesDelegatee(
				this,
				layout,
				new LoaderScheduleRowProviders(
						new SettingScheduleRowItemProvider(this)
				),
				new ScheduleTimeRowProviders(
						new SettingScheduleTimeRowItemProvider(this)
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
