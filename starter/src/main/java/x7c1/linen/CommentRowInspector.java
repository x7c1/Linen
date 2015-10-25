package x7c1.linen;

import android.content.Context;
import android.util.Log;
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
		final CommentRowHolder holder;
		if (convertView == null || convertView.getTag() == null){
			View layout = layoutInflater.inflate(R.layout.comment_row, parent, false);
			holder = new CommentRowHolder(
				layout,
				(TextView) layout.findViewById(R.id.comment_row_name),
				(TextView) layout.findViewById(R.id.comment_row_content)
			);
			layout.setTag(holder);
		} else {
			holder = (CommentRowHolder) convertView.getTag();
		}
		return holder;
	}

}
