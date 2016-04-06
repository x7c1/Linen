package x7c1.linen.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import x7c1.linen.R;
import x7c1.linen.base.BaseFragmentActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingMyChannelsLayout;
import x7c1.linen.modern.init.settings.my.MyChannelRowProviders;
import x7c1.linen.modern.init.settings.my.MyChannelsDelegatee;
import x7c1.linen.res.layout.SettingMyChannelCreateProvider;
import x7c1.linen.res.layout.SettingMyChannelRowFooterProvider;
import x7c1.linen.res.layout.SettingMyChannelRowItemProvider;
import x7c1.linen.res.layout.SettingMyChannelsLayoutProvider;
import x7c1.wheat.ancient.context.ContextualFactory;

public class MyChannelsActivity extends BaseFragmentActivity {

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
				new MyChannelRowProviders(
						new SettingMyChannelRowItemProvider(this),
						new SettingMyChannelRowFooterProvider(this)
				)
//				new SettingMyChannelRowProvider(this)
		);
		this.delegatee.setup();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.close();
	}
	@Override
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDirectChild(this);
	}
	private static class DialogFactory implements ContextualFactory<AlertDialog.Builder>{
		@Override
		public AlertDialog.Builder newInstance(Context context) {
			return new AlertDialog.Builder(context, R.style.AppAlertDialog);
		}
	}
}

