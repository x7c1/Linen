
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.glue.res.layout;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SettingChannelsRow extends RecyclerView.ViewHolder {

    public final TextView name;
    public final TextView description;
    public final TextView createdAt;

    public SettingChannelsRow(
        View itemView,
        TextView name,
        TextView description,
        TextView createdAt
    ){
        super(itemView);
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }
}