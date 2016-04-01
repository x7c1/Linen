package x7c1.linen.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import x7c1.linen.R;
import x7c1.linen.base.Control;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.activity.ActivityControl;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.res.layout.SettingMyChannelsLayout;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;
import x7c1.linen.modern.init.settings.my.MyChannelsDelegatee;
import x7c1.linen.res.layout.SettingMyChannelCreateProvider;
import x7c1.linen.res.layout.SettingMyChannelRowProvider;
import x7c1.linen.res.layout.SettingMyChannelsLayoutProvider;
import x7c1.wheat.ancient.context.ContextualFactory;

public class MyChannelsActivity
		extends FragmentActivity implements ActivityControl, ServiceControl {

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
				new DialogFactory(),
				SettingMyChannelCreateProvider.factory(),
				new SettingMyChannelRowProvider(this)
		);
		this.delegatee.setup();
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		TransitAnimations.forDirectChild(this).finish();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.close();
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
	private static class DialogFactory implements ContextualFactory<AlertDialog.Builder>{
		@Override
		public AlertDialog.Builder newInstance(Context context) {
			return new AlertDialog.Builder(context, R.style.AppAlertDialog);
		}
	}
}

