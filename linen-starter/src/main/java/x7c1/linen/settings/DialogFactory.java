package x7c1.linen.settings;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import x7c1.linen.R;
import x7c1.wheat.ancient.context.ContextualFactory;

public class DialogFactory implements ContextualFactory<AlertDialog.Builder> {
	@Override
	public AlertDialog.Builder newInstance(Context context) {
		return new AlertDialog.Builder(context, R.style.AppAlertDialog);
	}
}
