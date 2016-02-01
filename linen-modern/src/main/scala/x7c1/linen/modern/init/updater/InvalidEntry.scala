package x7c1.linen.modern.init.updater

sealed trait InvalidEntry

case class EmptyUrl() extends InvalidEntry

case class EmptyPublishedDate() extends InvalidEntry

case class Abort[A <: Throwable](cause: A) extends InvalidEntry
