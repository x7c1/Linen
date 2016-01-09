package x7c1.linen;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;

import x7c1.linen.glue.res.layout.MainLayout;
import x7c1.linen.modern.init.ContainerInitializer;
import x7c1.linen.res.layout.EntryDetailRowProvider;
import x7c1.linen.res.layout.EntryRowProvider;
import x7c1.linen.res.layout.MainLayoutProvider;
import x7c1.linen.res.layout.MenuRowItemProvider;
import x7c1.linen.res.layout.MenuRowLabelProvider;
import x7c1.linen.res.layout.SourceRowProvider;


public class MainActivity extends BaseActivity {

	private ContainerInitializer initializer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final MainLayout layout = new MainLayoutProvider(this).inflate(null, false);

		layout.sourceToolbar.setNavigationIcon(R.drawable.ic_action_menu);
		layout.sourceToolbar.inflateMenu(R.menu.menu_main);

		layout.entryToolbar.inflateMenu(R.menu.menu_main);
		layout.entryDetailToolbar.inflateMenu(R.menu.menu_main);

		setContentView(layout.itemView);

		this.initializer = new ContainerInitializer(
			this,
			layout,
			new MenuRowLabelProvider(this),
			new MenuRowItemProvider(this),
			new SourceRowProvider(this),
			new EntryRowProvider(this),
			new EntryDetailRowProvider(this)
		);
		initializer.setup();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		initializer.close();
	}

}
