package x7c1.linen;

import android.os.Bundle;
import android.view.KeyEvent;

import x7c1.linen.glue.res.layout.MainLayout;
import x7c1.linen.modern.init.unread.MenuRowProviders;
import x7c1.linen.modern.init.unread.UnreadItemsDelegatee;
import x7c1.linen.modern.init.unread.UnreadRowProviders;
import x7c1.linen.res.layout.MainLayoutProvider;
import x7c1.linen.res.layout.MenuRowLabelProvider;
import x7c1.linen.res.layout.MenuRowSeparatorProvider;
import x7c1.linen.res.layout.MenuRowTitleProvider;
import x7c1.linen.res.layout.UnreadDetailRowProvider;
import x7c1.linen.res.layout.UnreadOutlineRowProvider;
import x7c1.linen.res.layout.UnreadSourceRowProvider;


public class MainActivity extends BaseActivity {

	private UnreadItemsDelegatee initializer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final MainLayout layout = new MainLayoutProvider(this).inflate(null, false);

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
				new UnreadSourceRowProvider(this),
				new UnreadOutlineRowProvider(this),
				new UnreadDetailRowProvider(this)
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
}
