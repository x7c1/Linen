package x7c1.linen.interfaces;

import android.view.View;
import android.widget.TextView;

public class CommentRowHolder {

	public final View layout;
	public final TextView name;
	public final TextView content;

	public CommentRowHolder(View layout, TextView name, TextView content) {
		this.layout = layout;
		this.name = name;
		this.content = content;
	}
}
