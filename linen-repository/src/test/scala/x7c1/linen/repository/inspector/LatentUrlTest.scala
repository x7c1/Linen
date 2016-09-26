package x7c1.linen.repository.inspector

import org.scalatest.{FlatSpecLike, Matchers}

class LatentUrlTest extends FlatSpecLike with Matchers  {

  behavior of classOf[LatentUrl].getSimpleName

  it can "generate valid url" in {
    val Right(url) = LatentUrl.create("http://example.com/foo", "/bar.atom")
    url.full shouldBe "http://example.com/bar.atom"
  }
}
