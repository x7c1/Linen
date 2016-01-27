
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
import android.support.v7.widget.SwitchCompat;
import android.widget.SeekBar;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow;

public class SettingChannelSourcesRowProvider implements ViewHolderProvider<SettingChannelSourcesRow> {

    private final LayoutInflater inflater;

    public SettingChannelSourcesRowProvider(Context context){
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int layoutId(){
        return R.layout.setting_channel_sources_row;
    }

    @Override
    public SettingChannelSourcesRow inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SettingChannelSourcesRow inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.setting_channel_sources_row, parent, attachToRoot);
        return new SettingChannelSourcesRow(
            view,
            (TextView) view.findViewById(R.id.setting_channel_sources_row__title),
            (TextView) view.findViewById(R.id.setting_channel_sources_row__description),
            (android.support.v7.widget.SwitchCompat) view.findViewById(R.id.setting_channel_sources_row__switch_subscribe),
            (SeekBar) view.findViewById(R.id.setting_channel_sources_row__rating_bar),
            (TextView) view.findViewById(R.id.setting_channel_sources_row__rating_label)
        );
    }
}
