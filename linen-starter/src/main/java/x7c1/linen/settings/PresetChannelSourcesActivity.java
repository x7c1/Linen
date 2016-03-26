package x7c1.linen.settings;

import x7c1.linen.base.BaseActivity;
import x7c1.linen.base.TransitAnimation;
import x7c1.linen.base.TransitAnimations;

public class PresetChannelSourcesActivity extends BaseActivity {

	@Override
	protected TransitAnimation createTransitAnimation() {
		return TransitAnimations.forDescendant(this);
	}

	@Override
	public void finish() {
		finishFromChild();
	}
}
