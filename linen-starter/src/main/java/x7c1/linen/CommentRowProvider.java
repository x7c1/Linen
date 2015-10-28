package x7c1.linen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import x7c1.wheat.ancient.resource.LayoutProvider;
import x7c1.linen.glue.res.layout.CommentRowLayout;

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

		if (convertView == null || convertView.getTag(R.id.comment_row__name) == null){
			view = layoutInflater.inflate(R.layout.comment_row, parent, attachToRoot);
			name = (TextView) view.findViewById(R.id.comment_row__name);
			content = (TextView) view.findViewById(R.id.comment_row__content);

			view.setTag(R.id.comment_row__name, name);
			view.setTag(R.id.comment_row__content, content);
		} else {
			view = convertView;
			name = (TextView) view.getTag(R.id.comment_row__name);
			content = (TextView) view.getTag(R.id.comment_row__content);
		}
		return new CommentRowLayout(view, name, content);
	}

}
