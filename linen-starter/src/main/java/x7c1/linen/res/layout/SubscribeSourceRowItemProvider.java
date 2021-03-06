
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SubscribeSourceRowItem;

public class SubscribeSourceRowItemProvider implements ViewHolderProvider<SubscribeSourceRowItem> {

    private final LayoutInflater inflater;

    public SubscribeSourceRowItemProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SubscribeSourceRowItemProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.subscribe_source_row__item;
    }

    @Override
    public SubscribeSourceRowItem inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SubscribeSourceRowItem inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.subscribe_source_row__item, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public SubscribeSourceRowItem inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<SubscribeSourceRowItem> factory(){
        return new ViewHolderProviderFactory<SubscribeSourceRowItem>() {
            @Override
            public ViewHolderProvider<SubscribeSourceRowItem> create(LayoutInflater inflater){
                return new SubscribeSourceRowItemProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SubscribeSourceRowItem> create(Context context){
                return new SubscribeSourceRowItemProvider(context);
            }
            @Override
            public SubscribeSourceRowItem createViewHolder(View view){
                return new SubscribeSourceRowItem(
                    view,
                    (CheckBox) view.findViewById(R.id.subscribe_source_row__item__checkbox),
                    (TextView) view.findViewById(R.id.subscribe_source_row__item__label)
                );
            }
        };
    }
}
