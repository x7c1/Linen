package x7c1.linen.unread;

import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.base.BaseActivity;
import x7c1.linen.R;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.UnreadItemsLayout;
import x7c1.linen.modern.init.unread.DetailListProviders;
import x7c1.linen.modern.init.unread.MenuRowProviders;
import x7c1.linen.modern.init.unread.OutlineListProviders;
import x7c1.linen.modern.init.unread.SourceListProviders;
import x7c1.linen.modern.init.unread.UnreadItemsDelegatee;
import x7c1.linen.modern.init.unread.UnreadRowProviders;
import x7c1.linen.res.layout.UnreadItemsLayoutProvider;
import x7c1.linen.res.layout.MenuRowLabelProvider;
import x7c1.linen.res.layout.MenuRowSeparatorProvider;
import x7c1.linen.res.layout.MenuRowTitleProvider;
import x7c1.linen.res.layout.UnreadDetailRowEntryProvider;
import x7c1.linen.res.layout.UnreadDetailRowFooterProvider;
import x7c1.linen.res.layout.UnreadDetailRowSourceProvider;
import x7c1.linen.res.layout.UnreadOutlineRowEntryProvider;
import x7c1.linen.res.layout.UnreadOutlineRowFooterProvider;
import x7c1.linen.res.layout.UnreadOutlineRowSourceProvider;
import x7c1.linen.res.layout.UnreadSourceRowFooterProvider;
import x7c1.linen.res.layout.UnreadSourceRowItemProvider;


public class UnreadItemsActivity extends BaseActivity {

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
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forRoot(this);
	}
}