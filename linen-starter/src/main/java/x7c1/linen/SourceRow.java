package x7c1.linen;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SourceRow extends RecyclerView.ViewHolder {

	public final TextView title;
	public final TextView description;

	public SourceRow(View itemView, TextView title, TextView description) {
		super(itemView);
		this.title = title;
		this.description = description;
	}
}
