package x7c1.linen.interfaces;

import android.view.View;
import android.view.ViewGroup;

public interface ViewInspector<T> {
	T getOrInflate(View convertView, ViewGroup parent, boolean attachToRoot);
}
