package x7c1.linen.res;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SourceRow;
import x7c1.wheat.ancient.resource.ViewHolderProvider;

public class SourceRowProvider implements ViewHolderProvider<SourceRow> {
	private final LayoutInflater inflater;

	public SourceRowProvider(Context context) {
		inflater = LayoutInflater.from(context);
	}

	@Override
	public SourceRow inflate(ViewGroup parent, boolean attachToRoot) {
		View view = inflater.inflate(R.layout.source_row, parent, attachToRoot);
		return new SourceRow(
				view,
				(TextView) view.findViewById(R.id.source_row__title),
				(TextView) view.findViewById(R.id.source_row__description)
		);
	}

	@Override
	public SourceRow inflateOn(ViewGroup parent) {
		return inflate(parent, false);
	}
}
