package x7c1.linen.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import x7c1.linen.R;
import x7c1.linen.base.BaseFragmentActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SettingChannelOrderLayout;
import x7c1.linen.modern.init.settings.order.ChannelOrderDelegatee;
import x7c1.linen.modern.init.settings.order.ChannelOrderRowProviders;
import x7c1.linen.res.layout.SettingChannelOrderLayoutProvider;
import x7c1.linen.res.layout.SettingChannelOrderRowItemProvider;

public class ChannelOrderActivity extends BaseFragmentActivity {
	private ChannelOrderDelegatee delegatee = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SettingChannelOrderLayout layout =
				new SettingChannelOrderLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new ChannelOrderDelegatee(
				this,
				layout,
				new ChannelOrderRowProviders(
						new SettingChannelOrderRowItemProvider(this)
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

