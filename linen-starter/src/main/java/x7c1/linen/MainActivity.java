package x7c1.linen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import x7c1.linen.modern.SourceRowAdapter;
import x7c1.linen.res.layout.CommentRowLayoutProvider;
import x7c1.linen.res.layout.SourceRowProvider;
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

		RecyclerView leftView = (RecyclerView) findViewById(R.id.sample_left_list);
		leftView.setLayoutManager(new LinearLayoutManager(this));
		leftView.setAdapter(new SourceRowAdapter(new SourceRowProvider(this)));

		ListView rightListView = (ListView) findViewById(R.id.sample_right_list);

		View left = findViewById(R.id.swipe_layout_left);
		updateWidth(0.9, left);

		View center = findViewById(R.id.swipe_layout_center);
		updateWidth(0.8, center);

		View right = findViewById(R.id.swipe_layout_right);
		updateWidth(0.9, right);

		BaseAdapter adapter = new SampleAdapter(
				new CommentRowLayoutProvider(this),
				new CommentValuesProvider(this)
		);
		rightListView.setAdapter(adapter);

		final ViewGroup container = (LinearLayout) findViewById(R.id.swipe_container);
		final GestureDetector detector = forHorizontal(
				this, createListener(container, getDisplaySize()));

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
