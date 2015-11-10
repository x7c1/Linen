package x7c1.linen;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SourceRowAdapter extends RecyclerView.Adapter<SourceRowHolder>{

	private final LayoutInflater inflater;
	private final List<Source> sources;

	public SourceRowAdapter(Context context) {
		this.sources = createDummyList();
		this.inflater = LayoutInflater.from(context);
	}

	private List<Source> createDummyList(){
		List<Source> list = new ArrayList<>();
		for (int i = 0; i < 100; i++){
			list.add(new Source("sample-title-" + i, "sample-description-" + i));
		}
		return list;
	}

	@Override
	public SourceRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new SourceRowHolder(inflater.inflate(R.layout.source_row, parent, false));
	}

	@Override
	public void onBindViewHolder(SourceRowHolder holder, int position) {
		Source source = sources.get(position);
		holder.title.setText(source.getTitle());
		holder.description.setText(source.getDescription());
	}

	@Override
	public int getItemCount() {
		return sources.size();
	}


}
