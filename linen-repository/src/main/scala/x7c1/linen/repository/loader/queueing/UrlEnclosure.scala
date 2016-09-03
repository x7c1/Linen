package x7c1.linen.repository.loader.queueing

import java.net.URL

import x7c1.wheat.modern.queue.map.TrackableQueue.CanDump

trait UrlEnclosure {
  def raw: URL
}

object UrlEnclosure {

  implicit def canDump[A <: UrlEnclosure]: CanDump[A] = new CanDump[A] {
    override def dump(x: A): String = x.raw.toString
  }

}
