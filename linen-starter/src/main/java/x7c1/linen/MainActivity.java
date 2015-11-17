package x7c1.linen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;

import x7c1.linen.glue.res.layout.ActivityMain;
import x7c1.linen.modern.ContainerInitializer;
import x7c1.linen.modern.SampleAdapter;
import x7c1.linen.modern.SampleImpl;
import x7c1.linen.res.layout.ActivityMainProvider;
import x7c1.linen.res.layout.CommentRowLayoutProvider;
import x7c1.linen.res.layout.SourceRowProvider;
import x7c1.linen.res.values.CommentValuesProvider;

import static x7c1.linen.FlingDetector.forHorizontal;
import static x7c1.linen.PaneScroller.createListener;


public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActivityMain layout = new ActivityMainProvider(this).inflate(null, false);
		setContentView(layout.itemView);

		String str = new SampleImpl().getFoo(this);
		layout.sampleText.setText(str);

		ContainerInitializer initializer = new ContainerInitializer(
			this,
			layout,
			new SourceRowProvider(this)
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
