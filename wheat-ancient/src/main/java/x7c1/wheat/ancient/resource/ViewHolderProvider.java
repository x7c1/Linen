package x7c1.wheat.ancient.resource;

import android.view.ViewGroup;

public interface ViewHolderProvider<T> {

	int layoutId();

	T inflate(ViewGroup parent, boolean attachToRoot);

	T inflate();

	T inflateOn(ViewGroup parent);
}
