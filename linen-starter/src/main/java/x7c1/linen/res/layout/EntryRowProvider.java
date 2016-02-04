
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
import x7c1.linen.R;
import x7c1.linen.glue.res.layout.EntryRow;

public class EntryRowProvider implements ViewHolderProvider<EntryRow> {

    private final LayoutInflater inflater;

    public EntryRowProvider(Context context){
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int layoutId(){
        return R.layout.entry_row;
    }

    @Override
    public EntryRow inflateOn(ViewGroup parent){
        return inflate(parent, false);
    }

    @Override
    public EntryRow inflate(ViewGroup parent, boolean attachToRoot){
        View view = inflater.inflate(R.layout.entry_row, parent, attachToRoot);
        return new EntryRow(
            view,
            (TextView) view.findViewById(R.id.entry_row__title)
        );
    }
}
