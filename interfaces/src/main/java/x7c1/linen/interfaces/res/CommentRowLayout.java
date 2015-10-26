package x7c1.linen.interfaces.res;

import android.view.View;
import android.widget.TextView;

public class CommentRowLayout {

	public final View view;
	public final TextView name;
	public final TextView content;

	public CommentRowLayout(View view, TextView name, TextView content) {
		this.view = view;
		this.name = name;
		this.content = content;
	}
}
