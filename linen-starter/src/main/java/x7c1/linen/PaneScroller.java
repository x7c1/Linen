package x7c1.linen;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Scroller;

class PaneScroller implements Runnable {

	private final Scroller scroller;
	private final ViewGroup container;

	private int lastX = 0;

	public PaneScroller(Context context, ViewGroup container) {
		this.scroller = new Scroller(context);
		this.container = container;
	}
	public static FlingDetector.OnFlingListener createListener(ViewGroup container){
		return new Listener(container);
	}

	public void start(FlingDetector.FlingEvent event){
		int initialX = 0;

		/*
		int maxX = Integer.MAX_VALUE;
		int minX = Integer.MIN_VALUE;
		int maxX = 200;
		int minX = -200;
		scroller.fling(initialX, 0, initialVelocity, 0, minX, maxX, 0, 0);
		*/

		if (event.getVelocityX() > 0){
			scroller.startScroll(0, 0, 400, 0, 750);
		} else {
			scroller.startScroll(0, 0, -400, 0, 750);
		}

		Log.i("PaneScroller.start", "starting fling:" + initialX);
		Log.i("PaneScroller.start", "current scroll:" + container.getScrollX());

		lastX = initialX;
		container.post(this);
	}

	@Override
	public void run() {
		if (scroller.isFinished()){
			return;
		}
		boolean more = scroller.computeScrollOffset();
		int x = scroller.getCurrX();

		int diff = lastX - x;
		if (diff != 0){
			container.scrollBy(diff, 0);
			lastX = x;
		}
		if (more){
			container.post(this);
		}
	}

	private static class Listener implements FlingDetector.OnFlingListener {

		final private ViewGroup container;

		public Listener(ViewGroup container) {
			this.container = container;
		}

		@Override
		public boolean onFling(FlingDetector.FlingEvent event) {
			new PaneScroller(container.getContext(), container).start(event);
			return false;
		}
	}
}
