
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.glue.res.layout;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;

public class SettingScheduleRowItem extends SettingScheduleRow {

    public final TextView name;
    public final ImageButton menu;
    public final android.support.v7.widget.RecyclerView timeRanges;
    public final android.support.v7.widget.SwitchCompat enabled;
    public final TextView history;

    public SettingScheduleRowItem(
        View itemView,
        TextView name,
        ImageButton menu,
        android.support.v7.widget.RecyclerView timeRanges,
        android.support.v7.widget.SwitchCompat enabled,
        TextView history
    ){
        super(itemView);
        this.name = name;
        this.menu = menu;
        this.timeRanges = timeRanges;
        this.enabled = enabled;
        this.history = history;
    }
}
