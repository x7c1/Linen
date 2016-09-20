package x7c1.wheat.modern.queue.map

import java.net.URL

import org.scalatest.{FlatSpecLike, Matchers}

class GroupingQueueTest extends FlatSpecLike with Matchers {
  it can "enqueue and dequeue" in {
    val queue = GroupingQueue.groupBy[String, URL](_.getHost)
    val x1 = queue.enqueue(new URL("http://1.example.com/1-1"))
    x1 shouldBe false

    val x2 = queue.enqueue(new URL("http://1.example.com/1-2"))
    x2 shouldBe true

    val x3 = queue.enqueue(new URL("http://1.example.com/1-3"))
    x3 shouldBe true

    val y1 = queue.dequeue(new URL("http://1.example.com/"))
    y1 shouldBe Some(new URL("http://1.example.com/1-2"))

    val y2 = queue.dequeue(new URL("http://1.example.com/"))
    y2 shouldBe Some(new URL("http://1.example.com/1-3"))

    val y3 = queue.dequeue(new URL("http://1.example.com/"))
    y3 shouldBe None
  }

}
