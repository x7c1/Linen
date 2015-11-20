
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
import android.widget.LinearLayout;
import x7c1.linen.glue.res.view.CustomSwipeToRefresh;
import android.support.v7.widget.RecyclerView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.ActivityMain;

public class ActivityMainProvider implements ViewHolderProvider<ActivityMain> {

    private final LayoutInflater inflater;

    public ActivityMainProvider(Context context){
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ActivityMain inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public ActivityMain inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.activity_main, parent, attachToRoot);
        return new ActivityMain(
            view,
            (TextView) view.findViewById(R.id.activity_main__sample_text),
            (LinearLayout) view.findViewById(R.id.activity_main__swipe_container),
            (x7c1.linen.glue.res.view.CustomSwipeToRefresh) view.findViewById(R.id.activity_main__swipe_layout_left),
            (android.support.v7.widget.RecyclerView) view.findViewById(R.id.activity_main__sample_left_list),
            (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.activity_main__swipe_layout_center),
            (android.support.v7.widget.RecyclerView) view.findViewById(R.id.activity_main__sample_center_list),
            (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.activity_main__swipe_layout_right),
            (ListView) view.findViewById(R.id.activity_main__sample_right_list),
            (View) view.findViewById(R.id.activity_main__dummy_surface)
        );
    }
}