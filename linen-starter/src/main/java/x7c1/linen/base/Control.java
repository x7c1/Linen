package x7c1.linen.base;

import x7c1.linen.UpdaterService;
import x7c1.linen.dev.CreateRecordsActivity;
import x7c1.linen.glue.activity.ActivityLabel;
import x7c1.linen.glue.service.ServiceLabel;
import x7c1.linen.settings.PresetChannelsActivity;
import x7c1.linen.settings.MyChannelSourcesActivity;
import x7c1.linen.settings.MyChannelsActivity;

import static java.lang.String.format;

public class Control {

	public static Class<?> getActivityClassOf(ActivityLabel label) {
		final Class<?> klass;
		switch(label){
			case SettingMyChannels:
				klass = MyChannelsActivity.class;
				break;
			case SettingMyChannelSources:
				klass = MyChannelSourcesActivity.class;
				break;
			case SettingPresetChannels:
				klass = PresetChannelsActivity.class;
				break;
			case CreateRecords:
				klass = CreateRecordsActivity.class;
				break;
			default:
				throw new IllegalArgumentException(format("unknown activity label: %s", label));
		}
		return klass;
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
}
