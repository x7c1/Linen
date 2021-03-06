
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
import x7c1.linen.glue.res.layout.UnreadDetailRowFooter;

public class UnreadDetailRowFooterProvider implements ViewHolderProvider<UnreadDetailRowFooter> {

    private final LayoutInflater inflater;

    public UnreadDetailRowFooterProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public UnreadDetailRowFooterProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.unread_detail_row__footer;
    }

    @Override
    public UnreadDetailRowFooter inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public UnreadDetailRowFooter inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.unread_detail_row__footer, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public UnreadDetailRowFooter inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<UnreadDetailRowFooter> factory(){
        return new ViewHolderProviderFactory<UnreadDetailRowFooter>() {
            @Override
            public ViewHolderProvider<UnreadDetailRowFooter> create(LayoutInflater inflater){
                return new UnreadDetailRowFooterProvider(inflater);
            }
            @Override
            public ViewHolderProvider<UnreadDetailRowFooter> create(Context context){
                return new UnreadDetailRowFooterProvider(context);
            }
            @Override
            public UnreadDetailRowFooter createViewHolder(View view){
                return new UnreadDetailRowFooter(
                    view
                );
            }
        };
    }
}
