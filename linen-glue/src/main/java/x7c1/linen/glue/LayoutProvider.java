package x7c1.linen.glue;

import android.view.View;
import android.view.ViewGroup;

public interface LayoutProvider<T> {
	T getOrInflate(View convertView, ViewGroup parent, boolean attachToRoot);
}
