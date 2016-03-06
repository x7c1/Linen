package x7c1.linen.modern.accessor

import java.net.URL

object EntryUrl {
  def apply(url: String): EntryUrl = {
    new EntryUrl(new URL(url))
  }
}

class EntryUrl private (url: URL) {
  def host: String = url.getHost
  def raw: String = url.toExternalForm
}
