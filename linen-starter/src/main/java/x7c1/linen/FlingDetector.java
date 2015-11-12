package x7c1.linen;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import static java.lang.Math.abs;

class FlingDetector {

	public static GestureDetector forHorizontal(Context context, OnFlingListener listener){
		return new GestureDetector(context, new HorizontalListener(listener));
	}

	public static interface OnFlingListener {
		boolean onFling(FlingEvent event);
	}

	public static class FlingEvent {

		private final float velocityX;
		private final float velocityY;

		private FlingEvent(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			this.velocityX = velocityX;
			this.velocityY = velocityY;
		}

		public float getVelocityX() {
			return velocityX;
		}

		public float getVelocityY() {
			return velocityY;
		}
	}

	private static class HorizontalListener implements OnGestureListener{

		final private OnFlingListener listener;

		public HorizontalListener(OnFlingListener listener) {
			this.listener = listener;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			final boolean consumed;
			if (abs(velocityX) > abs(velocityY)){
				FlingEvent event = new FlingEvent(e1, e2, velocityX, velocityY);
				consumed = listener.onFling(event);
			} else {
				consumed = false;
			}
			return consumed;
		}
	}
}
