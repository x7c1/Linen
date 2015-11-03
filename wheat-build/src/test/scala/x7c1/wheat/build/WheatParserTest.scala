package x7c1.wheat.build

import org.scalatest.{FlatSpecLike, Matchers}

class WheatParserTest extends FlatSpecLike with Matchers {
  behavior of "WheatParser"

  it can "convert snake to camel" in {
    val Right(camel) = WheatParser.toCamelCase("abcd_ef_ghi")
    camel shouldBe "AbcdEfGhi"
  }

  it should "fail to invalid file name" in {
    val Left(e) = WheatParser.toCamelCase("0xyz_abcd_ef_ghi.xml")
    e shouldBe a[WheatParserError]
  }

}
