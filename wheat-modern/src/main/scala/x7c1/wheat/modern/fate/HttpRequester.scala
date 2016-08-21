package x7c1.wheat.modern.fate

import java.io.{BufferedInputStream, InputStreamReader, Reader}
import java.net.{HttpURLConnection, URL}

import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext, using}
import x7c1.wheat.modern.kinds.Fate


object HttpRequester {
  def apply[X: HasContext, L: ErrorLike](): HttpRequester[X, L] = {
    new HttpRequester()
  }
}

class HttpRequester[X: HasContext, L: ErrorLike] private {

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
