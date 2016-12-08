
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.SettingSourceAttach;

public class SettingSourceAttachProvider implements ViewHolderProvider<SettingSourceAttach> {

    private final LayoutInflater inflater;

    public SettingSourceAttachProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public SettingSourceAttachProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.setting_source_attach;
    }

    @Override
    public SettingSourceAttach inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public SettingSourceAttach inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.setting_source_attach, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public SettingSourceAttach inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<SettingSourceAttach> factory(){
        return new ViewHolderProviderFactory<SettingSourceAttach>() {
            @Override
            public ViewHolderProvider<SettingSourceAttach> create(LayoutInflater inflater){
                return new SettingSourceAttachProvider(inflater);
            }
            @Override
            public ViewHolderProvider<SettingSourceAttach> create(Context context){
                return new SettingSourceAttachProvider(context);
            }
            @Override
            public SettingSourceAttach createViewHolder(View view){
                return new SettingSourceAttach(
                    view,
                    (android.support.v7.widget.RecyclerView) view.findViewById(R.id.setting_source_attach__channels)
                );
            }
        };
    }
}
