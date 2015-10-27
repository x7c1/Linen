package x7c1.linen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import x7c1.linen.interfaces.LayoutProvider;
import x7c1.linen.interfaces.res.CommentRowLayout;

public class CommentRowProvider implements LayoutProvider<CommentRowLayout> {

	private final LayoutInflater layoutInflater;

	public CommentRowProvider(Context context) {
		this.layoutInflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public CommentRowLayout getOrInflate(View convertView, ViewGroup parent, boolean attachToRoot) {
		final View view;
		final TextView name;
		final TextView content;

		if (convertView == null || convertView.getTag(R.id.comment_row_name) == null){
			view = layoutInflater.inflate(R.layout.comment_row, parent, attachToRoot);
			name = (TextView) view.findViewById(R.id.comment_row_name);
			content = (TextView) view.findViewById(R.id.comment_row_content);

			view.setTag(R.id.comment_row_name, name);
			view.setTag(R.id.comment_row_content, content);
		} else {
			view = convertView;
			name = (TextView) view.getTag(R.id.comment_row_name);
			content = (TextView) view.getTag(R.id.comment_row_content);
		}
		return new CommentRowLayout(view, name, content);
	}

}
