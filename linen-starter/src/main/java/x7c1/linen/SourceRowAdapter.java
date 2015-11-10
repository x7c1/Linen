package x7c1.linen;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import x7c1.wheat.ancient.resource.ViewHolderProvider;

public class SourceRowAdapter extends RecyclerView.Adapter<SourceRow>{

	private final List<Source> sources;
	private final ViewHolderProvider<SourceRow> provider;

	public SourceRowAdapter(ViewHolderProvider<SourceRow> provider) {
		this.provider = provider;
		this.sources = createDummyList();
	}

	private List<Source> createDummyList(){
		List<Source> list = new ArrayList<>();
		for (int i = 0; i < 100; i++){
			list.add(new Source("sample-title-" + i, "sample-description-" + i));
		}
		return list;
	}

	@Override
	public SourceRow onCreateViewHolder(ViewGroup parent, int viewType) {
		return provider.inflateOn(parent);
	}

	@Override
	public void onBindViewHolder(SourceRow holder, int position) {
		Source source = sources.get(position);
		holder.title.setText(source.getTitle());
		holder.description.setText(source.getDescription());
	}

	@Override
	public int getItemCount() {
		return sources.size();
	}


}
