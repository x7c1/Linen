
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
import x7c1.wheat.ancient.resource.ViewHolderProvider;
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory;
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.MenuRowTitle;

public class MenuRowTitleProvider implements ViewHolderProvider<MenuRowTitle> {

    private final LayoutInflater inflater;

    public MenuRowTitleProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public MenuRowTitleProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.menu_row__title;
    }

    @Override
    public MenuRowTitle inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public MenuRowTitle inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.menu_row__title, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public MenuRowTitle inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<MenuRowTitle> factory(){
        return new ViewHolderProviderFactory<MenuRowTitle>() {
            @Override
            public ViewHolderProvider<MenuRowTitle> create(LayoutInflater inflater){
                return new MenuRowTitleProvider(inflater);
            }
            @Override
            public ViewHolderProvider<MenuRowTitle> create(Context context){
                return new MenuRowTitleProvider(context);
            }
            @Override
            public MenuRowTitle createViewHolder(View view){
                return new MenuRowTitle(
                    view,
                    (TextView) view.findViewById(R.id.menu_row__title__text)
                );
            }
        };
    }
}
