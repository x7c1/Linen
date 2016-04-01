package x7c1.wheat.ancient.context;

import android.content.Context;

import java.io.Serializable;

public interface ContextualFactory<A> extends Serializable {
	A newInstance(Context context);
}
