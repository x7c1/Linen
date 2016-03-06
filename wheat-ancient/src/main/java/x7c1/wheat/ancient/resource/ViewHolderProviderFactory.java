package x7c1.wheat.ancient.resource;

import android.content.Context;
import android.view.LayoutInflater;

import java.io.Serializable;

public interface ViewHolderProviderFactory<T> extends Serializable {
	ViewHolderProvider<T> create(LayoutInflater inflater);
	ViewHolderProvider<T> create(Context context);
}
