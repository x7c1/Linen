
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.glue.res.layout;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.SwitchCompat;
import android.widget.SeekBar;
import android.widget.ImageButton;

public class SettingChannelSourcesRow extends RecyclerView.ViewHolder {

    public final TextView title;
    public final TextView description;
    public final android.support.v7.widget.SwitchCompat switchSubscribe;
    public final SeekBar ratingBar;
    public final TextView ratingLabel;
    public final ImageButton sync;

    public SettingChannelSourcesRow(
        View itemView,
        TextView title,
        TextView description,
        android.support.v7.widget.SwitchCompat switchSubscribe,
        SeekBar ratingBar,
        TextView ratingLabel,
        ImageButton sync
    ){
        super(itemView);
        this.title = title;
        this.description = description;
        this.switchSubscribe = switchSubscribe;
        this.ratingBar = ratingBar;
        this.ratingLabel = ratingLabel;
        this.sync = sync;
    }
}
