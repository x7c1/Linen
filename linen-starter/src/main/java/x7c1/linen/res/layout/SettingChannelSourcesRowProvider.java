
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
import android.widget.ImageButton;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow;

public class SettingChannelSourcesRowProvider implements ViewHolderProvider<SettingChannelSourcesRow> {

    private final LayoutInflater inflater;

    public SettingChannelSourcesRowProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SettingChannelSourcesRowProvider(LayoutInflater inflater){
        this.inflater = inflater;
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
        return factory().createViewHolder(view);
    }

    public static ViewHolderProviderFactory<SettingChannelSourcesRow> factory(){
        return new ViewHolderProviderFactory<SettingChannelSourcesRow>() {
            @Override
            public ViewHolderProvider<SettingChannelSourcesRow> create(LayoutInflater inflater){
                return new SettingChannelSourcesRowProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SettingChannelSourcesRow> create(Context context){
                return new SettingChannelSourcesRowProvider(context);
            }
            @Override
            public SettingChannelSourcesRow createViewHolder(View view){
                return new SettingChannelSourcesRow(
                    view,
                    (TextView) view.findViewById(R.id.setting_channel_sources_row__title),
                    (TextView) view.findViewById(R.id.setting_channel_sources_row__description),
                    (android.support.v7.widget.SwitchCompat) view.findViewById(R.id.setting_channel_sources_row__switch_subscribe),
                    (SeekBar) view.findViewById(R.id.setting_channel_sources_row__rating_bar),
                    (TextView) view.findViewById(R.id.setting_channel_sources_row__rating_value),
                    (TextView) view.findViewById(R.id.setting_channel_sources_row__rating_label),
                    (ImageButton) view.findViewById(R.id.setting_channel_sources_row__sync)
                );
            }
        };
    }
}
