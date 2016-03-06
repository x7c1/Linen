
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingPresetTabSelected;

public class SettingPresetTabSelectedProvider implements ViewHolderProvider<SettingPresetTabSelected> {

    private final LayoutInflater inflater;

    public SettingPresetTabSelectedProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SettingPresetTabSelectedProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.setting_preset_tab__selected;
    }

    @Override
    public SettingPresetTabSelected inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SettingPresetTabSelected inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.setting_preset_tab__selected, parent, attachToRoot);
        return new SettingPresetTabSelected(
            view
        );
    }

    public static ViewHolderProviderFactory<SettingPresetTabSelected> factory(){
        return new ViewHolderProviderFactory<SettingPresetTabSelected>() {
            @Override
            public ViewHolderProvider<SettingPresetTabSelected> create(LayoutInflater inflater){
                return new SettingPresetTabSelectedProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SettingPresetTabSelected> create(Context context){
                return new SettingPresetTabSelectedProvider(context);
            }
        };
    }
}
