package x7c1.linen;

import android.app.Activity;

public class ChildActivityDelegatee {
	private Activity activity;

	public ChildActivityDelegatee(Activity activity) {
		this.activity = activity;
	}

	public void start(){
		activity.overridePendingTransition(R.animator.slide_in, R.animator.slide_out);
	}
	public void finish(){
		activity.overridePendingTransition(R.animator.back_slide_in, R.animator.back_slide_out);
	}
}