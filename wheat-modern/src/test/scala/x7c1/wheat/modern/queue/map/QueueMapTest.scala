package x7c1.wheat.modern.queue.map


import java.net.URL

import org.scalatest.{FlatSpecLike, Matchers}

class QueueMapTest extends FlatSpecLike with Matchers {

  it can "enqueue" in {
    val map: QueueMap[String, URL] = QueueMap(_.getHost)
    val urls = Seq(
      "http://1.example.com/1-1",
      "http://2.example.com/2-1",
      "http://1.example.com/1-2",
      "http://1.example.com/1-3",
      "http://2.example.com/2-2",
      "http://2.example.com/2-3"
    )
    urls.map(new URL(_)).foreach(map.enqueue)

    {
      val url1 = map.dequeue("1.example.com").map(_.toExternalForm)
      val url2 = map.dequeue("1.example.com").map(_.toExternalForm)
      val url3 = map.dequeue("1.example.com").map(_.toExternalForm)

      Seq(url1, url2, url3).flatten shouldBe Seq(
        "http://1.example.com/1-1",
        "http://1.example.com/1-2",
        "http://1.example.com/1-3"
      )
    }

    {
      val url1 = map.dequeue("2.example.com").map(_.toExternalForm)
      map.headOption("2.example.com").map(_.toExternalForm) shouldBe
        Some("http://2.example.com/2-2")

      val url2 = map.dequeue("2.example.com").map(_.toExternalForm)
      map.headOption("2.example.com").map(_.toExternalForm) shouldBe
        Some("http://2.example.com/2-3")

      val url3 = map.dequeue("2.example.com").map(_.toExternalForm)
      map.headOption("2.example.com") shouldBe None

      Seq(url1, url2, url3).flatten shouldBe Seq(
        "http://2.example.com/2-1",
        "http://2.example.com/2-2",
        "http://2.example.com/2-3"
      )
    }

  }
}
