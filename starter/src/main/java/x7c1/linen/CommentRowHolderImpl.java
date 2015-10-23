package x7c1.linen;

import android.view.View;
import android.widget.TextView;

import x7c1.linen.interfaces.CommentRowHolder;

public class CommentRowHolderImpl implements CommentRowHolder {

	private View view;

	public CommentRowHolderImpl(View view) {
		this.view = view;
	}

	@Override
	public TextView getName() {
		return (TextView) view.findViewById(R.id.comment_row_name);
	}

	@Override
	public TextView getContent() {
		return (TextView) view.findViewById(R.id.comment_row_content);
	}

	@Override
	public View getLayout() {
		return view;
	}

}
