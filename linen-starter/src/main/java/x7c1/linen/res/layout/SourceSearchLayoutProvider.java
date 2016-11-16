
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.FloatingActionButton;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SourceSearchLayout;

public class SourceSearchLayoutProvider implements ViewHolderProvider<SourceSearchLayout> {

    private final LayoutInflater inflater;

    public SourceSearchLayoutProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SourceSearchLayoutProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.source_search_layout;
    }

    @Override
    public SourceSearchLayout inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SourceSearchLayout inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.source_search_layout, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public SourceSearchLayout inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<SourceSearchLayout> factory(){
        return new ViewHolderProviderFactory<SourceSearchLayout>() {
            @Override
            public ViewHolderProvider<SourceSearchLayout> create(LayoutInflater inflater){
                return new SourceSearchLayoutProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SourceSearchLayout> create(Context context){
                return new SourceSearchLayoutProvider(context);
            }
            @Override
            public SourceSearchLayout createViewHolder(View view){
                return new SourceSearchLayout(
                    view,
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.source_search_layout__toolbar),
                    (android.support.v7.widget.RecyclerView) view.findViewById(R.id.source_search_layout__reports),
                    (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.source_search_layout__button_to_create)
                );
            }
        };
    }
}
