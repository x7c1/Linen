package x7c1.linen.unread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.R;
import x7c1.linen.base.Control;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.activity.ActivityControl;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.res.layout.UnreadItemsLayout;
import x7c1.linen.glue.service.ServiceControl;
import x7c1.linen.glue.service.ServiceLabel;
import x7c1.linen.modern.init.unread.MenuRowProviders;
import x7c1.linen.modern.init.unread.SourceListProviders;
import x7c1.linen.modern.init.unread.UnreadItemsDelegatee;
import x7c1.linen.modern.init.unread.UnreadRowProviders;
import x7c1.linen.modern.init.unread.entry.DetailListProviders;
import x7c1.linen.modern.init.unread.entry.OutlineListProviders;
import x7c1.linen.res.layout.MenuRowLabelProvider;
import x7c1.linen.res.layout.MenuRowSeparatorProvider;
import x7c1.linen.res.layout.MenuRowTitleProvider;
import x7c1.linen.res.layout.UnreadDetailRowEntryProvider;
import x7c1.linen.res.layout.UnreadDetailRowFooterProvider;
import x7c1.linen.res.layout.UnreadDetailRowSourceProvider;
import x7c1.linen.res.layout.UnreadItemsLayoutProvider;
import x7c1.linen.res.layout.UnreadOutlineRowEntryProvider;
import x7c1.linen.res.layout.UnreadOutlineRowFooterProvider;
import x7c1.linen.res.layout.UnreadOutlineRowSourceProvider;
import x7c1.linen.res.layout.UnreadSourceRowFooterProvider;
import x7c1.linen.res.layout.UnreadSourceRowItemProvider;


public class UnreadItemsActivity extends Activity implements ActivityControl, ServiceControl {

	private UnreadItemsDelegatee initializer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final UnreadItemsLayout layout = new UnreadItemsLayoutProvider(this).inflate(null, false);

		layout.sourceToolbar.setNavigationIcon(R.drawable.ic_action_menu);
		layout.sourceToolbar.inflateMenu(R.menu.menu_main);

		layout.entryToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		layout.entryToolbar.inflateMenu(R.menu.menu_main);

		layout.entryDetailToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		layout.entryDetailToolbar.inflateMenu(R.menu.menu_main);

		setContentView(layout.itemView);

		this.initializer = new UnreadItemsDelegatee(
			this,
			layout,
			new MenuRowProviders(
				new MenuRowTitleProvider(this),
				new MenuRowLabelProvider(this),
				new MenuRowSeparatorProvider(this)
			),
			new UnreadRowProviders(
				new SourceListProviders(
						new UnreadSourceRowItemProvider(this),
						new UnreadSourceRowFooterProvider(this)
				),
				new OutlineListProviders(
						new UnreadOutlineRowSourceProvider(this),
						new UnreadOutlineRowEntryProvider(this),
						new UnreadOutlineRowFooterProvider(this)
				),
				new DetailListProviders(
						new UnreadDetailRowSourceProvider(this),
						new UnreadDetailRowEntryProvider(this),
						new UnreadDetailRowFooterProvider(this)
				)
			)
		);
		initializer.setup();
	}

	@Override
	protected void onPause() {
		super.onPause();
		initializer.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		initializer.close();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return initializer.onKeyDown(keyCode, event) ||
				super.onKeyDown(keyCode, event);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		TransitAnimations.forRoot(this).start();
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
