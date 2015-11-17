package x7c1.linen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import x7c1.linen.glue.res.layout.ActivityMain;
import x7c1.linen.modern.EntriesArea;
import x7c1.linen.modern.PaneContainer;
import x7c1.linen.modern.SampleAdapter;
import x7c1.linen.modern.SampleImpl;
import x7c1.linen.modern.SourceRowAdapter;
import x7c1.linen.modern.SourceSelectObserver;
import x7c1.linen.modern.SourceStore;
import x7c1.linen.modern.SourcesArea;
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

		layout.sampleCenterList.setLayoutManager(new LinearLayoutManager(this));

		layout.sampleLeftList.setLayoutManager(new LinearLayoutManager(this));
		layout.sampleLeftList.setAdapter(new SourceRowAdapter(
				new SourceStore(),
				new SourceSelectObserver(
						new PaneContainer(
								layout.swipeContainer,
								new SourcesArea(layout.sampleLeftList, 0),
								new EntriesArea(layout.sampleCenterList, 864)
						)
				),
				new SourceRowProvider(this)));

		updateWidth(0.9, layout.swipeLayoutLeft);

		updateWidth(0.8, layout.swipeLayoutCenter);

		updateWidth(0.9, layout.swipeLayoutRight);

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

	private void updateWidth(double ratio, View view){
		ViewGroup.LayoutParams params = view.getLayoutParams();
		Point size = getDisplaySize();
		params.width = (int) (ratio * size.x);
		view.setLayoutParams(params);
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
