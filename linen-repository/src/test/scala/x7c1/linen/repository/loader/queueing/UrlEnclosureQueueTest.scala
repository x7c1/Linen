package x7c1.linen.repository.loader.queueing

import java.net.URL

import org.scalatest.{FlatSpecLike, Matchers}

class UrlEnclosureQueueTest extends FlatSpecLike with Matchers {

  it can "enqueue same domain URL" in {
    val url1 = SampleUrl(new URL("http://example.com/1"))
    val url2 = SampleUrl(new URL("http://example.com/2"))
    val url3 = SampleUrl(new URL("http://example.com/3"))

    val queue = UrlEnclosureQueue[SampleUrl]()

    queue.enqueue(url1) shouldBe false
    queue.enqueue(url2) shouldBe true
    queue.enqueue(url3) shouldBe true

    val f1 = queue.dequeue(url1)
    f1.map(_.raw.toExternalForm) shouldBe Option("http://example.com/2")

    val f2 = queue.dequeue(f1.get)
    f2.map(_.raw.toExternalForm) shouldBe Option("http://example.com/3")

    val f3 = queue.dequeue(f2.get)
    f3 shouldBe None
  }

  it can "enqueue different domain URL" in {
    val url1 = SampleUrl(new URL("http://sub1.example.com/1"))
    val url2 = SampleUrl(new URL("http://sub2.example.com/2"))

    val url3_1 = SampleUrl(new URL("http://sub3.example.com/3"))
    val url3_2 = SampleUrl(new URL("http://sub3.example.com/4"))

    val queue = UrlEnclosureQueue[SampleUrl]()

    queue.enqueue(url1) shouldBe false
    queue.enqueue(url2) shouldBe false
    queue.enqueue(url3_1) shouldBe false
    queue.enqueue(url3_2) shouldBe true

    val f1 = queue.dequeue(url1)
    f1 shouldBe None

    val f2 = queue.dequeue(url2)
    f2 shouldBe None

    val f3 = queue.dequeue(url3_1)
    f3.map(_.raw.toExternalForm) shouldBe Option("http://sub3.example.com/4")

    val f4 = queue.dequeue(f3.get)
    f4 shouldBe None
  }
}
