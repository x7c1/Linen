
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
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.MainLayout;

public class MainLayoutProvider implements ViewHolderProvider<MainLayout> {

    private final LayoutInflater inflater;

    public MainLayoutProvider(Context context){
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MainLayout inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public MainLayout inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.main_layout, parent, attachToRoot);
        return new MainLayout(
            view,
            (LinearLayout) view.findViewById(R.id.main_layout__menu_area),
            (LinearLayout) view.findViewById(R.id.activity_main__swipe_container),
            (android.support.design.widget.CoordinatorLayout) view.findViewById(R.id.activity_main__swipe_layout_left),
            (android.support.v7.widget.Toolbar) view.findViewById(R.id.activity_main__source_toolbar),
            (android.support.v7.widget.RecyclerView) view.findViewById(R.id.activity_main__sample_left_list),
            (LinearLayout) view.findViewById(R.id.activity_main__swipe_layout_center),
            (android.support.v7.widget.Toolbar) view.findViewById(R.id.activity_main__entry_toolbar),
            (android.support.v7.widget.RecyclerView) view.findViewById(R.id.activity_main__sample_center_list),
            (LinearLayout) view.findViewById(R.id.activity_main__swipe_layout_right),
            (android.support.v7.widget.Toolbar) view.findViewById(R.id.activity_main__entry_detail_toolbar),
            (android.support.v7.widget.RecyclerView) view.findViewById(R.id.activity_main__sample_right_list),
            (TextView) view.findViewById(R.id.activity_main__sample_text),
            (View) view.findViewById(R.id.activity_main__dummy_surface)
        );
    }
}