package x7c1.linen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import x7c1.linen.interfaces.CommentRowHolder;
import x7c1.linen.interfaces.ViewInspector;

public class CommentRowInspector implements ViewInspector<CommentRowHolder> {

	private final LayoutInflater layoutInflater;

	public CommentRowInspector(Context context) {
		this.layoutInflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public CommentRowHolder createHolder(View convertView, ViewGroup parent) {
		final View layout;
		if (convertView == null){
			layout = layoutInflater.inflate(R.layout.comment_row, parent, false);
		} else {
			layout = convertView;
		}
		return new CommentRowHolderImpl(
			layout,
			(TextView) layout.findViewById(R.id.comment_row_name),
			(TextView) layout.findViewById(R.id.comment_row_content)
		);
	}
	private static class CommentRowHolderImpl implements CommentRowHolder {

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
}
