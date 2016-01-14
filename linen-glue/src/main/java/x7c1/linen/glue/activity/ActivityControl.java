package x7c1.linen.glue.activity;

import android.content.Intent;

public interface ActivityControl {
	void startActivityBy(Intent intent);
	Class<?> getClassOf(ActivityLabel label);
}
