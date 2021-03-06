
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
import x7c1.linen.glue.res.layout.SourceSearchRowSourceNotFound;

public class SourceSearchRowSourceNotFoundProvider implements ViewHolderProvider<SourceSearchRowSourceNotFound> {

    private final LayoutInflater inflater;

    public SourceSearchRowSourceNotFoundProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SourceSearchRowSourceNotFoundProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.source_search_row__source_not_found;
    }

    @Override
    public SourceSearchRowSourceNotFound inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SourceSearchRowSourceNotFound inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.source_search_row__source_not_found, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public SourceSearchRowSourceNotFound inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<SourceSearchRowSourceNotFound> factory(){
        return new ViewHolderProviderFactory<SourceSearchRowSourceNotFound>() {
            @Override
            public ViewHolderProvider<SourceSearchRowSourceNotFound> create(LayoutInflater inflater){
                return new SourceSearchRowSourceNotFoundProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SourceSearchRowSourceNotFound> create(Context context){
                return new SourceSearchRowSourceNotFoundProvider(context);
            }
            @Override
            public SourceSearchRowSourceNotFound createViewHolder(View view){
                return new SourceSearchRowSourceNotFound(
                    view,
                    (TextView) view.findViewById(R.id.source_search_row__source_not_found__title),
                    (TextView) view.findViewById(R.id.source_search_row__source_not_found__url)
                );
            }
        };
    }
}
