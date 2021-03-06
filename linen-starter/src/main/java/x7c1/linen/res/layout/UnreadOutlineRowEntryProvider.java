
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
import x7c1.linen.glue.res.layout.UnreadOutlineRowEntry;

public class UnreadOutlineRowEntryProvider implements ViewHolderProvider<UnreadOutlineRowEntry> {

    private final LayoutInflater inflater;

    public UnreadOutlineRowEntryProvider(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    public UnreadOutlineRowEntryProvider(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int layoutId(){
        return R.layout.unread_outline_row__entry;
    }

    @Override
    public UnreadOutlineRowEntry inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public UnreadOutlineRowEntry inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.unread_outline_row__entry, parent, attachToRoot);
        return factory().createViewHolder(view);
    }

    @Override
    public UnreadOutlineRowEntry inflate(){
        return inflate(null, false);
    }

    public static ViewHolderProviderFactory<UnreadOutlineRowEntry> factory(){
        return new ViewHolderProviderFactory<UnreadOutlineRowEntry>() {
            @Override
            public ViewHolderProvider<UnreadOutlineRowEntry> create(LayoutInflater inflater){
                return new UnreadOutlineRowEntryProvider(inflater);
            }
            @Override
            public ViewHolderProvider<UnreadOutlineRowEntry> create(Context context){
                return new UnreadOutlineRowEntryProvider(context);
            }
            @Override
            public UnreadOutlineRowEntry createViewHolder(View view){
                return new UnreadOutlineRowEntry(
                    view,
                    (TextView) view.findViewById(R.id.unread_outline_row__entry__title)
                );
            }
        };
    }
}
