package x7c1.linen.tools;

import android.os.Bundle;

import x7c1.linen.R;
import x7c1.linen.base.BaseActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SourceSearchLayout;
import x7c1.linen.modern.init.inspector.SearchReportRowProviders;
import x7c1.linen.modern.init.inspector.SourceSearchDelegatee;
import x7c1.linen.res.layout.SourceSearchLayoutProvider;
import x7c1.linen.res.layout.SourceSearchRowLoadingErrorItemProvider;
import x7c1.linen.res.layout.SourceSearchRowLoadingErrorLabelProvider;
import x7c1.linen.res.layout.SourceSearchRowSourceItemProvider;
import x7c1.linen.res.layout.SourceSearchRowSourceLabelProvider;

public class SourceSearchActivity extends BaseActivity {
	private SourceSearchDelegatee delegatee = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SourceSearchLayout layout =
				new SourceSearchLayoutProvider(this).inflate(null, false);

		layout.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
		setContentView(layout.itemView);

		this.delegatee = new SourceSearchDelegatee(
				this,
				layout,
				new SearchReportRowProviders(
						new SourceSearchRowSourceLabelProvider(this),
						new SourceSearchRowSourceItemProvider(this),
						new SourceSearchRowLoadingErrorLabelProvider(this),
						new SourceSearchRowLoadingErrorItemProvider(this)
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
