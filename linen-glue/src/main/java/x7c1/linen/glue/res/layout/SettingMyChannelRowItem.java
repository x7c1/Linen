
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.glue.res.layout;

import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.SwitchCompat;

public class SettingMyChannelRowItem extends SettingMyChannelRow {

    public final TextView name;
    public final TextView description;
    public final android.support.v7.widget.SwitchCompat switchSubscribe;
    public final TextView edit;
    public final TextView sources;

    public SettingMyChannelRowItem(
        View itemView,
        TextView name,
        TextView description,
        android.support.v7.widget.SwitchCompat switchSubscribe,
        TextView edit,
        TextView sources
    ){
        super(itemView);
        this.name = name;
        this.description = description;
        this.switchSubscribe = switchSubscribe;
        this.edit = edit;
        this.sources = sources;
    }
}
