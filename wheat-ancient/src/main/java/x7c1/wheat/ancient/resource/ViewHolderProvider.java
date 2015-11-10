package x7c1.wheat.ancient.resource;

import android.view.ViewGroup;

public interface ViewHolderProvider<T> {

	T inflate(ViewGroup parent, boolean attachToRoot);

	T inflateOn(ViewGroup parent);
}
