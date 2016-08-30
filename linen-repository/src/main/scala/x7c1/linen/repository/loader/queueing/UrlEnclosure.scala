package x7c1.linen.repository.loader.queueing

import java.net.URL

import x7c1.wheat.modern.queue.map.TrackableQueue.CanDump

trait UrlEnclosure {
  def raw: URL
}

object UrlEnclosure {

  implicit object canDump extends CanDump[UrlEnclosure] {
    override def dump(x: UrlEnclosure): String = x.raw.toString
  }

}
