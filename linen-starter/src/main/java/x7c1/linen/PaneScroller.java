package x7c1.linen;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.Scroller;

class PaneScroller implements Runnable {

	private final Scroller scroller;
	private final ViewGroup container;

	public PaneScroller(ViewGroup container) {
		this.scroller = new Scroller(container.getContext());
		this.container = container;
	}

	public static FlingDetector.OnFlingListener createListener(ViewGroup container){
		return new Listener(container);
	}

	public void start(FlingDetector.FlingEvent event){

		/*
		int maxX = Integer.MAX_VALUE;
		int minX = Integer.MIN_VALUE;
		int maxX = 200;
		int minX = -200;
		scroller.fling(initialX, 0, initialVelocity, 0, minX, maxX, 0, 0);
		*/

		int current = container.getScrollX();
		float velocityX = event.getVelocityX();

		final int dx;
		if (velocityX < 0){
			if (current < 0){
				dx = 0 - current;
			} else if (current < 1000){
				dx = 1000 - current;
			} else if (current < 2000) {
				dx = 2000 - current;
			} else {
				dx = 0;
			}
			Log.d("PaneScroller.start", "v<0 dx:" + dx);
		} else {
			if (current > 2000){
				dx = 2000 - current;
			} else if (current > 1000) {
				dx = 1000 - current;
			} else if (current > 0){
				dx = 0 - current;
			} else {
				dx = 0;
			}
			Log.d("PaneScroller.start", "v>0 dx" + dx);
		}
		Log.d("PaneScroller.start", "current scroll:" + current);

		scroller.startScroll(current, 0, dx, 0, 500);

		container.post(this);
	}

	@Override
	public void run() {
		if (scroller.isFinished()){
			return;
		}
		boolean more = scroller.computeScrollOffset();
		int x = scroller.getCurrX();
		container.scrollTo(x, 0);
		if (more){
			container.post(this);
		}
	}

	private static class Listener implements FlingDetector.OnFlingListener {

		private final PaneScroller scroller;

		public Listener(ViewGroup container) {
			scroller = new PaneScroller(container);
		}

		@Override
		public boolean onFling(FlingDetector.FlingEvent event) {
			scroller.start(event);
			return false;
		}
	}
}
