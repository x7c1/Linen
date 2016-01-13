package x7c1.linen;

import android.content.Context;
import android.content.Intent;

import x7c1.linen.dev.CreateRecordsActivity;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.service.ServiceLabel;

import static java.lang.String.format;

public class Control {

	public static Class<?> getActivityClassOf(ActivityLabel label) {
		final Class<?> klass;
		switch(label){
			case SettingChannels:
				klass = SettingChannelsActivity.class;
				break;
			case CreateRecords:
				klass = CreateRecordsActivity.class;
				break;
			default:
				throw new IllegalArgumentException(format("unknown activity label: %s", label));
		}
		return klass;
	}

	public static Intent getActivityIntentOf(Context context, ActivityLabel label) {
		final Intent intent;
		switch (label){
			case SettingChannels:
				intent = new Intent(context, SettingChannelsActivity.class);
				break;
			case CreateRecords:
				intent = new Intent(context, CreateRecordsActivity.class);
				break;
			default:
				throw new IllegalArgumentException(format("unknown activity label: %s", label));
		}
		return intent;
	}

	public static Class<?> getServiceClassOf(ServiceLabel label) {
		final Class<?> klass;
		switch(label){
			case Updater:
				klass = UpdaterService.class;
				break;
			default:
				throw new IllegalArgumentException(format("unknown service label: %s", label));
		}
		return klass;
	}

	public static Intent getServiceIntentOf(Context context, ServiceLabel label) {
		final Intent intent;
		switch(label){
			case Updater:
				intent = new Intent(context, UpdaterService.class);
				break;
			default:
				throw new IllegalArgumentException(format("unknown service label: %s", label));
		}
		return intent;
	}
}
