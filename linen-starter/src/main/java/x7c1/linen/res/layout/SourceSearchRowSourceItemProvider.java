
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SourceSearchRowSourceItem;

public class SourceSearchRowSourceItemProvider implements ViewHolderProvider<SourceSearchRowSourceItem> {

    private final LayoutInflater inflater;

    public SourceSearchRowSourceItemProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SourceSearchRowSourceItemProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.source_search_row__source_item;
    }

    @Override
    public SourceSearchRowSourceItem inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SourceSearchRowSourceItem inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.source_search_row__source_item, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    public static ViewHolderProviderFactory<SourceSearchRowSourceItem> factory(){
        return new ViewHolderProviderFactory<SourceSearchRowSourceItem>() {
            @Override
            public ViewHolderProvider<SourceSearchRowSourceItem> create(LayoutInflater inflater){
                return new SourceSearchRowSourceItemProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SourceSearchRowSourceItem> create(Context context){
                return new SourceSearchRowSourceItemProvider(context);
            }
            @Override
            public SourceSearchRowSourceItem createViewHolder(View view){
                return new SourceSearchRowSourceItem(
                    view,
                    (TextView) view.findViewById(R.id.source_search_row__source_item__title),
                    (TextView) view.findViewById(R.id.source_search_row__source_item__url),
                    (TextView) view.findViewById(R.id.source_search_row__source_item__subscribe),
                    (TextView) view.findViewById(R.id.source_search_row__source_item__entries)
                );
            }
        };
    }
}