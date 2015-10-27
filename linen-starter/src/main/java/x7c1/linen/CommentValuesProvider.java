package x7c1.linen;

import android.content.Context;

import x7c1.wheat.ancient.resource.ValuesProvider;
import x7c1.linen.glue.res.values.CommentValues;

public class CommentValuesProvider implements ValuesProvider<CommentValues>{
	private final CommentValues comment;

	public CommentValuesProvider(Context context) {
		comment = new CommentValues(
				context,
				R.string.comment__name_clicked,
				R.string.comment__content_clicked
				);
	}

	@Override
	public CommentValues get() {
		return comment;
	}
}
