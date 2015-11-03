package x7c1.wheat.build

import org.scalatest.{FlatSpecLike, Matchers}

class WheatParserTest extends FlatSpecLike with Matchers {
  behavior of "WheatParser"

  it can "convert snake to camel" in {
    val Right(camel) = WheatParser.camelize("abcd_ef_ghi")
    camel shouldBe "AbcdEfGhi"
  }

  it can "camelize strings except for head string" in {
    val Right(x0) = WheatParser.camelizeTail("abcd_ef_ghi")
    x0 shouldBe "abcdEfGhi"
  }

  it should "fail to invalid file name" in {
    val Left(e) = WheatParser.camelize("0xyz_abcd_ef_ghi.xml")
    e shouldBe a[WheatParserError]
  }

}
