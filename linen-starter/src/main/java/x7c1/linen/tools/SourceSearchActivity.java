package x7c1.linen.tools;

import android.os.Bundle;

import x7c1.linen.R;
import x7c1.linen.base.BaseFragmentActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;
import x7c1.linen.glue.res.layout.SourceSearchLayout;
import x7c1.linen.modern.init.inspector.SearchReportRowProviders;
import x7c1.linen.modern.init.inspector.SourceSearchDelegatee;
import x7c1.linen.res.layout.SourceSearchLayoutProvider;
import x7c1.linen.res.layout.SourceSearchRowClientErrorProvider;
import x7c1.linen.res.layout.SourceSearchRowFooterProvider;
import x7c1.linen.res.layout.SourceSearchRowLabelProvider;
import x7c1.linen.res.layout.SourceSearchRowOriginErrorProvider;
import x7c1.linen.res.layout.SourceSearchRowSourceItemProvider;
import x7c1.linen.res.layout.SourceSearchRowSourceNotFoundProvider;
import x7c1.linen.res.layout.SourceSearchStartProvider;
import x7c1.linen.settings.DialogFactory;

public class SourceSearchActivity extends BaseFragmentActivity {
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
				new DialogFactory(),
				SourceSearchStartProvider.factory(),
				new SearchReportRowProviders(
						new SourceSearchRowLabelProvider(this),
						new SourceSearchRowSourceItemProvider(this),
						new SourceSearchRowOriginErrorProvider(this),
						new SourceSearchRowSourceNotFoundProvider(this),
						new SourceSearchRowClientErrorProvider(this),
						new SourceSearchRowFooterProvider(this)
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
