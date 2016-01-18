package x7c1.wheat.macros.intent

sealed trait IntentExpanderError

case class UnknownAction(action: String) extends IntentExpanderError