package x7c1.linen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import x7c1.linen.interfaces.CommentRowHolder;
import x7c1.linen.interfaces.ViewInspector;

public class CommentRowInspector implements ViewInspector<CommentRowHolder> {

	private final LayoutInflater layoutInflater;
	private final Context context;

	public CommentRowInspector(Context context) {
		this.context = context;
		this.layoutInflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public CommentRowHolder createHolder(ViewGroup parent) {
		View view = layoutInflater.inflate(R.layout.comment_row, parent, false);
		return new CommentRowHolderImpl(view);
	}
}
