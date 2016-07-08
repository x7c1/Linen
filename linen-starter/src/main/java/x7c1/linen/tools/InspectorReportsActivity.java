package x7c1.linen.tools;

import android.os.Bundle;

import x7c1.linen.base.BaseActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.InspectorLayout;
import x7c1.linen.modern.init.inspector.InspectorReportsDelegatee;
import x7c1.linen.modern.init.inspector.InspectorRowProviders;
import x7c1.linen.res.layout.InspectorLayoutProvider;
import x7c1.linen.res.layout.InspectorRowSourceItemProvider;

public class InspectorReportsActivity extends BaseActivity {
	private InspectorReportsDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final InspectorLayout layout =
				new InspectorLayoutProvider(this).inflate(null, false);

		setContentView(layout.itemView);
		this.delegatee = new InspectorReportsDelegatee(
				this,
				layout,
				new InspectorRowProviders(
						new InspectorRowSourceItemProvider(this)
				)
		);
		this.delegatee.onCreate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		delegatee.onDestroy();
	}

	@Override
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDirectChild(this);
	}
}
