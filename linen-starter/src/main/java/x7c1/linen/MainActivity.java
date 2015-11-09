package x7c1.linen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import x7c1.linen.modern.SampleAdapter;
import x7c1.linen.modern.SampleImpl;
import x7c1.linen.res.layout.CommentRowLayoutProvider;
import x7c1.linen.res.values.CommentValuesProvider;

import static x7c1.linen.FlingDetector.forHorizontal;
import static x7c1.linen.PaneScroller.createListener;


public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView view = (TextView) findViewById(R.id.sample_text);
		String str = new SampleImpl().getFoo(this);
		view.setText(str);

		ListView listView = (ListView) findViewById(R.id.sample_right_list);

		View right = findViewById(R.id.swipe_layout_sample);
		right.setLayoutParams(changeParams(right.getLayoutParams()));

		BaseAdapter adapter = new SampleAdapter(
				new CommentRowLayoutProvider(this),
				new CommentValuesProvider(this)
		);
		listView.setAdapter(adapter);

		final ViewGroup container = (LinearLayout) findViewById(R.id.swipe_container);
		final SwipeRefreshLayout target = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_left);

		target.post(new Runnable() {
			@Override
			public void run() {
				Log.e("hoge", "" + container.getScrollY());
				container.scrollTo(0, 0);
			}
		});
		final GestureDetector detector = forHorizontal(this, createListener(container));

		View.OnTouchListener listener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				detector.onTouchEvent(event);
				container.dispatchTouchEvent(event);
				return false;
			}
		};

		View target0 = findViewById(R.id.dummy_surface);
		target0.setLongClickable(true);
		target0.setOnTouchListener(listener);
	}

	private ViewGroup.LayoutParams changeParams(ViewGroup.LayoutParams params){
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		params.width = size.x;
		return params;
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
