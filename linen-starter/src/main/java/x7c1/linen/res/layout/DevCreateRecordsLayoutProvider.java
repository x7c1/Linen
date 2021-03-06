
/**
 * This file is automatically generated by wheat-build.
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */

package x7c1.linen.res.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout;

public class DevCreateRecordsLayoutProvider implements ViewHolderProvider<DevCreateRecordsLayout> {

    private final LayoutInflater inflater;

    public DevCreateRecordsLayoutProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public DevCreateRecordsLayoutProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.dev_create_records_layout;
    }

    @Override
    public DevCreateRecordsLayout inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public DevCreateRecordsLayout inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.dev_create_records_layout, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public DevCreateRecordsLayout inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<DevCreateRecordsLayout> factory(){
        return new ViewHolderProviderFactory<DevCreateRecordsLayout>() {
            @Override
            public ViewHolderProvider<DevCreateRecordsLayout> create(LayoutInflater inflater){
                return new DevCreateRecordsLayoutProvider(inflater);
            }
            @Override
            public ViewHolderProvider<DevCreateRecordsLayout> create(Context context){
                return new DevCreateRecordsLayoutProvider(context);
            }
            @Override
            public DevCreateRecordsLayout createViewHolder(View view){
                return new DevCreateRecordsLayout(
                    view,
                    (android.support.v7.widget.Toolbar) view.findViewById(R.id.dev_create_records_layout__toolbar),
                    (Button) view.findViewById(R.id.dev_create_records_layout__select_channels),
                    (TextView) view.findViewById(R.id.dev_create_records_layout__selected_channels),
                    (Button) view.findViewById(R.id.dev_create_records_layout__create_dummy_sources),
                    (Button) view.findViewById(R.id.dev_create_records_layout__create_dummies),
                    (Button) view.findViewById(R.id.dev_create_records_layout__create_preset),
                    (Button) view.findViewById(R.id._dev_create_records_preset__create_preset_jp),
                    (Button) view.findViewById(R.id._dev_create_records_preset__create_preset_en),
                    (Button) view.findViewById(R.id._dev_init_records__mark_all_as_unread),
                    (Button) view.findViewById(R.id._dev_init_records__delete_database)
                );
            }
        };
    }
}
