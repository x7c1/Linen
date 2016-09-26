package x7c1.linen.repository.loader.queueing


trait UrlReceiver extends PartialFunction[UrlEnclosure, Unit]

object UrlReceiver {
  def apply(f: PartialFunction[UrlEnclosure, Unit]): UrlReceiver = {
    new UrlReceiver {
      override def isDefinedAt(x: UrlEnclosure): Boolean = f.isDefinedAt(x)
      override def apply(v1: UrlEnclosure): Unit = f(v1)
    }
  }
}
