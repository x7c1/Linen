package x7c1.linen;

import android.view.View;
import android.widget.TextView;

import x7c1.linen.interfaces.CommentRowHolder;

public class CommentRowHolderImpl implements CommentRowHolder {

	private final View layout;
	private final TextView name;
	private final TextView content;

	public CommentRowHolderImpl(View layout, TextView name, TextView content) {
		this.layout = layout;
		this.name = name;
		this.content = content;
	}

	@Override
	public TextView name() {
		return name;
	}

	@Override
	public TextView content() {
		return content;
	}

	@Override
	public View layout() {
		return layout;
	}

}
