package x7c1.linen.settings;

import android.os.Bundle;

import x7c1.linen.BaseActivity;

public class PresetChannelsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void finish() {
		finishFromChild();
	}
}
