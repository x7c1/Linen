package x7c1.linen;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import x7c1.linen.glue.res.layout.MainLayout;
import x7c1.linen.modern.ContainerInitializer;
import x7c1.linen.res.layout.EntryDetailRowProvider;
import x7c1.linen.res.layout.EntryRowProvider;
import x7c1.linen.res.layout.MainLayoutProvider;
import x7c1.linen.res.layout.SourceRowProvider;

import static x7c1.linen.FlingDetector.forHorizontal;
import static x7c1.linen.PaneScroller.createListener;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final MainLayout layout = new MainLayoutProvider(this).inflate(null, false);

		layout.sourceToolbar.setTitle("Technology");
		layout.sourceToolbar.setNavigationIcon(R.drawable.ic_action_menu);
		layout.sourceToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(getClass().getName(), v.getClass().getName());

				DrawerLayout x = (DrawerLayout) layout.itemView.findViewById(R.id.drawer_layout__root);
				x.openDrawer(GravityCompat.START);
			}
		});
		layout.sourceToolbar.inflateMenu(R.menu.menu_main);
		layout.entryToolbar.inflateMenu(R.menu.menu_main);
		layout.entryDetailToolbar.inflateMenu(R.menu.menu_main);

		setContentView(layout.itemView);

		ContainerInitializer initializer = new ContainerInitializer(
			this,
			layout,
			new SourceRowProvider(this),
			new EntryRowProvider(this),
			new EntryDetailRowProvider(this)
		);
		initializer.setup();

		final GestureDetector detector = forHorizontal(
				this, createListener(layout.swipeContainer, getDisplaySize()));

		View.OnTouchListener listener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event) ||
					layout.swipeContainer.dispatchTouchEvent(event);
			}
		};

		layout.dummySurface.setLongClickable(true);
		layout.dummySurface.setOnTouchListener(listener);
	}

	private Point getDisplaySize(){
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

}
