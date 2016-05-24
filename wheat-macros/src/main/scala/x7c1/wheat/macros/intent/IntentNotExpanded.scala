package x7c1.wheat.macros.intent

sealed trait IntentNotExpanded {
  def message: String
}

case class NoIntent() extends IntentNotExpanded {
  override def message: String = "empty intent"
}

case class NoIntentAction() extends IntentNotExpanded {
  override def message: String = "empty intent action"
}

case class UnknownAction(action: String) extends IntentNotExpanded {
  override def message: String = s"unknown intent action: $action"
}

case class ExtraNotFound(key: String) extends IntentNotExpanded {
  override def message: String = s"required extra not found: $key"
}
