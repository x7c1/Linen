package x7c1.linen;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomSwipeToRefresh extends SwipeRefreshLayout {

	public CustomSwipeToRefresh(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		final boolean consumed;
		if (event.getY() < 750){
			consumed = super.onInterceptTouchEvent(event);
		} else {
			consumed = false;
		}
		return consumed;
	}
}
