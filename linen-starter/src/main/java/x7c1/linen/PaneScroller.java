package x7c1.linen;

import android.graphics.Point;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import x7c1.linen.FlingDetector.FlingEvent;
import x7c1.linen.FlingDetector.OnFlingListener;

import static java.lang.Math.max;

class PaneScroller implements Runnable {

	private final Scroller scroller;
	private final ViewGroup container;
	private final Point displaySize;

	public PaneScroller(ViewGroup container, Point displaySize) {
		this.displaySize = displaySize;
		this.scroller = new Scroller(container.getContext());
		this.container = container;
	}

	public static OnFlingListener createListener(ViewGroup container, Point displaySize){
		return new Listener(container, displaySize);
	}

	public void start(FlingEvent event){
		int current = container.getScrollX();
		float velocityX = event.getVelocityX();

		final int dx;
		if (velocityX < 0){
			dx = getNext();
			Log.d("PaneScroller.start", "v<0 dx:" + dx);
		} else {
			dx = getPrevious();
			Log.d("PaneScroller.start", "v>0 dx:" + dx);
		}
		Log.d("PaneScroller.start", "current scroll:" + current);

		scroller.startScroll(current, 0, dx, 0, 350);

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

	private List<Integer> getPositions(){
		List<Integer> positions = new ArrayList<>();

		int display = displaySize.x;
		int length = container.getChildCount();
		for (int i = 0, start = 0; i < length; i++){
			int width = container.getChildAt(i).getWidth();
			int position = (i == length - 1) ?
				(start - (display - width)):
				(start - (display - width) / 2);

			start += width;
			positions.add(max(0, position));
		}
		return positions;
	}
	private int getNext(){
		List<Integer> positions = getPositions();

		int current = container.getScrollX();
		for (int i : positions){
			int diff = i - current;
			if (diff > 0) return diff;
		}
		return 0;
	}
	private int getPrevious(){
		List<Integer> positions = getPositions();
		Collections.reverse(positions);

		int current = container.getScrollX();
		for (int i : positions){
			int diff = i - current;
			if (diff < 0) return diff;
		}
		return 0;
	}
	private static class Listener implements OnFlingListener {

		private final PaneScroller scroller;

		public Listener(ViewGroup container, Point displaySize) {
			scroller = new PaneScroller(container, displaySize);
		}

		@Override
		public boolean onFling(FlingEvent event) {
			scroller.start(event);
			return false;
		}
	}
}
