package x7c1.linen.modern.init.updater

sealed trait EntryNotLoaded

case class EmptyUrl() extends EntryNotLoaded

case class EmptyPublishedDate() extends EntryNotLoaded

case class Abort[A <: Throwable](cause: A) extends EntryNotLoaded
