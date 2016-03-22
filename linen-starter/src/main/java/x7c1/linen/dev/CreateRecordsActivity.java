package x7c1.linen.dev;

import android.os.Bundle;

import x7c1.linen.BaseActivity;
import x7c1.linen.R;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout;
import x7c1.linen.modern.init.dev.CreateRecordsDelegatee;
import x7c1.linen.res.layout.DevCreateRecordsLayoutProvider;

public class CreateRecordsActivity extends BaseActivity {
	private CreateRecordsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final DevCreateRecordsLayout layout =
				new DevCreateRecordsLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new CreateRecordsDelegatee(this, layout);
		this.delegatee.setup();
	}
	@Override
	public void onBackPressed() {
		onBackPressedAtChild();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.close();
	}
	@Override
	public void finish() {
		finishFromChild();
	}

	@Override
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDirectChild(this);
	}
}
