package x7c1.wheat.modern.fate

import java.io.{BufferedInputStream, InputStreamReader, Reader}
import java.net.{HttpURLConnection, URL}

import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext, using}
import x7c1.wheat.modern.kinds.Fate


class HttpRequestFate[X: HasContext, L: ErrorLike] {

  private val future = FutureFate.hold[X, L]

  def readerOf(url: URL): Fate[X, L, Reader] = {
    for {
      connection <- future right {
        val connection = url.openConnection().asInstanceOf[HttpURLConnection]
        connection setRequestMethod "GET"
        connection
      }
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      reader
    }
  }
}
