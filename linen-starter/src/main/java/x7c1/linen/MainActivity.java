package x7c1.linen;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;

import x7c1.linen.glue.res.layout.ActivityMain;
import x7c1.linen.modern.ContainerInitializer;
import x7c1.linen.modern.SampleAdapter;
import x7c1.linen.res.layout.ActivityMainProvider;
import x7c1.linen.res.layout.CommentRowLayoutProvider;
import x7c1.linen.res.layout.EntryRowProvider;
import x7c1.linen.res.layout.SourceRowProvider;
import x7c1.linen.res.values.CommentValuesProvider;

import static x7c1.linen.FlingDetector.forHorizontal;
import static x7c1.linen.PaneScroller.createListener;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActivityMain layout = new ActivityMainProvider(this).inflate(null, false);
		layout.sourceToolbar.setTitle("Technology");
		layout.sourceToolbar.inflateMenu(R.menu.menu_main);


//		new SupportMenuInflater(this).inflate(R.menu.menu_main, layout.sourceToolbar.getMenu());


		setContentView(layout.itemView);

		ContainerInitializer initializer = new ContainerInitializer(
			this,
			layout,
			new SourceRowProvider(this),
			new EntryRowProvider(this)
		);
		initializer.setup();

		BaseAdapter adapter = new SampleAdapter(
				new CommentRowLayoutProvider(this),
				new CommentValuesProvider(this)
		);
		layout.sampleRightList.setAdapter(adapter);

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
