
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingPresetTabAll;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;

public class SettingPresetTabAllProvider implements ViewHolderProvider<SettingPresetTabAll> {

    private final LayoutInflater inflater;

    public SettingPresetTabAllProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SettingPresetTabAllProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.setting_preset_tab__all;
    }

    @Override
    public SettingPresetTabAll inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SettingPresetTabAll inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.setting_preset_tab__all, parent, attachToRoot);
        return new SettingPresetTabAll(
            view,
            (android.support.v7.widget.RecyclerView) view.findViewById(R.id.setting_preset_tab__all__channel_list)
        );
    }

    public static ViewHolderProviderFactory<SettingPresetTabAll> factory(){
        return new ViewHolderProviderFactory<SettingPresetTabAll>() {
            @Override
            public ViewHolderProvider<SettingPresetTabAll> create(LayoutInflater inflater){
                return new SettingPresetTabAllProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SettingPresetTabAll> create(Context context){
                return new SettingPresetTabAllProvider(context);
            }
            @Override
            public SettingPresetTabAll createViewHolder(View view) {
                return new SettingPresetTabAll(
                        view,
                        (android.support.v7.widget.RecyclerView) view.findViewById(R.id.setting_preset_tab__all__channel_list)
                );
            }
        };
    }
}
