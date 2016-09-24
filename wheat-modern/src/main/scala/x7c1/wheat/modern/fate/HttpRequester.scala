package x7c1.wheat.modern.fate

import java.io.{BufferedInputStream, InputStreamReader, Reader}
import java.net.HttpURLConnection.{HTTP_MOVED_PERM, HTTP_MOVED_TEMP}
import java.net.{HttpURLConnection, URL}

import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext, using}
import x7c1.wheat.modern.kinds.Fate


object HttpRequester {
  def apply[X: HasContext, L: ErrorLike](): HttpRequester[X, L] = {
    new HttpRequester()
  }
}

class HttpRequester[X: HasContext, L: ErrorLike] private {

  private val provide = FutureFate.hold[X, L]

  def readerOf(url: URL): Fate[X, L, Reader] = {
    for {
      connection <- provide right {
        val connection = openConnection(url)
        connection setRequestMethod "GET"
        connection
      }
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      reader
    }
  }

  private def openConnection(url: URL): HttpURLConnection = {
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.getResponseCode match {
      case HTTP_MOVED_PERM | HTTP_MOVED_TEMP =>
        val next = new URL(url, connection getHeaderField "Location")
        next.openConnection().asInstanceOf[HttpURLConnection]
      case _ =>
        connection
    }
  }
}
