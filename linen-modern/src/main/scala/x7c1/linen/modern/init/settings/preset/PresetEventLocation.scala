package x7c1.linen.modern.init.settings.preset

sealed trait PresetEventLocation extends Serializable

case object PresetTabSelected extends PresetEventLocation

case object PresetTabAll extends PresetEventLocation
