package x7c1.linen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import x7c1.linen.modern.SampleAdapter;
import x7c1.linen.modern.SampleImpl;
import x7c1.linen.res.layout.CommentRowLayoutProvider;
import x7c1.linen.res.values.CommentValuesProvider;


public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView view = (TextView) findViewById(R.id.sample_text);

		String str = new SampleImpl().getFoo(this);

		ListView listView = (ListView) findViewById(R.id.swipe_list);

		BaseAdapter adapter = new SampleAdapter(
				new CommentRowLayoutProvider(this),
				new CommentValuesProvider(this)
		);
		listView.setAdapter(adapter);

		view.setText(str);
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
