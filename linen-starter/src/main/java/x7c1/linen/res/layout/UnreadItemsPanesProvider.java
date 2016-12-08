
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.FloatingActionButton;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.UnreadItemsPanes;

public class UnreadItemsPanesProvider implements ViewHolderProvider<UnreadItemsPanes> {

    private final LayoutInflater inflater;

    public UnreadItemsPanesProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public UnreadItemsPanesProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.unread_items_panes;
    }

    @Override
    public UnreadItemsPanes inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public UnreadItemsPanes inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.unread_items_panes, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public UnreadItemsPanes inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<UnreadItemsPanes> factory(){
        return new ViewHolderProviderFactory<UnreadItemsPanes>() {
            @Override
            public ViewHolderProvider<UnreadItemsPanes> create(LayoutInflater inflater){
                return new UnreadItemsPanesProvider(inflater);
            }
            @Override
            public ViewHolderProvider<UnreadItemsPanes> create(Context context){
                return new UnreadItemsPanesProvider(context);
            }
            @Override
            public UnreadItemsPanes createViewHolder(View view){
                return new UnreadItemsPanes(
                    view,
                    (LinearLayout) view.findViewById(R.id.unread_items_panes__pane_container),
                    (RelativeLayout) view.findViewById(R.id.unread_items_panes__source_area),
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.unread_items_panes__source_toolbar),
                    (android.support.v7.widget.RecyclerView) view.findViewById(R.id.unread_items_panes__source_list),
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.unread_items_panes__source_bottom_bar),
                    (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.unread_items_panes__source_to_next),
                    (RelativeLayout) view.findViewById(R.id.unread_items_panes__entry_area),
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.unread_items_panes__entry_toolbar),
                    (android.support.v7.widget.RecyclerView) view.findViewById(R.id.unread_items_panes__entry_list),
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.unread_items_panes__entry_bottom_bar),
                    (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.unread_items_panes__entry_to_next),
                    (RelativeLayout) view.findViewById(R.id.unread_items_panes__entry_detail_area),
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.unread_items_panes__entry_detail_toolbar),
                    (android.support.v7.widget.RecyclerView) view.findViewById(R.id.unread_items_panes__entry_detail_list),
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.unread_items_panes__detail_bottom_bar),
                    (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.unread_items_panes__detail_to_next)
                );
            }
        };
    }
}
