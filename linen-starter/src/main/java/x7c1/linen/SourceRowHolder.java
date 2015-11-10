package x7c1.linen;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SourceRowHolder extends RecyclerView.ViewHolder {

	public final TextView title;
	public final TextView description;

	public SourceRowHolder(View itemView) {
		super(itemView);
		title = (TextView) itemView.findViewById(R.id.source_row__title);
		description = (TextView) itemView.findViewById(R.id.source_row__description);
	}
}
