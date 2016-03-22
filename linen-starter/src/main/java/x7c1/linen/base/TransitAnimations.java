package x7c1.linen.base;

import android.app.Activity;

import x7c1.linen.R;

public class TransitAnimations {
	public static TransitAnimation forRoot(Activity activity){
		return new AnimationImpl(activity,
				R.animator.h_slide_in,
				R.animator.h_slide_out,
				R.animator.h_back_slide_in,
				R.animator.h_back_slide_out
		);
	}
	public static TransitAnimation forDirectChild(Activity activity){
		return new AnimationImpl(activity,
				R.animator.h2_slide_in,
				R.animator.h2_slide_out,
				R.animator.h_back_slide_in,
				R.animator.h_back_slide_out
		);
	}
	public static TransitAnimation forDescendant(Activity activity){
		return new AnimationImpl(activity,
				R.animator.h2_slide_in,
				R.animator.h2_slide_out,
				R.animator.h2_back_slide_in,
				R.animator.h2_back_slide_out
		);
	}
	private static class AnimationImpl implements TransitAnimation {
		private final Activity activity;
		private final int startEnter;
		private final int startExit;
		private final int finishEnter;
		private final int finishExit;

		public AnimationImpl(Activity activity, int startEnter, int startExit, int finishEnter, int finishExit) {
			this.activity = activity;
			this.startEnter = startEnter;
			this.startExit = startExit;
			this.finishEnter = finishEnter;
			this.finishExit = finishExit;
		}
		@Override
		public void start() {
			activity.overridePendingTransition(startEnter, startExit);
		}
		@Override
		public void finish() {
			activity.overridePendingTransition(finishEnter, finishExit);
		}
	}
}
