package x7c1.wheat.build

import org.scalatest.{FlatSpecLike, Matchers}

class WheatParserTest extends FlatSpecLike with Matchers {
  behavior of "WheatParser"

  it can "camelize strings except for head string" in {
    val Right(x0) = WheatParser.camelizeTail("abcd_ef_ghi")
    x0 shouldBe "abcdEfGhi"
  }
}
