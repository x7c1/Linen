package x7c1.wheat.macros.intent

sealed trait IntentNotExpanded

case class NoIntent() extends IntentNotExpanded

case class NoIntentAction() extends IntentNotExpanded

case class UnknownAction(action: String) extends IntentNotExpanded

case class ExtraNotFound(key: String) extends IntentNotExpanded
