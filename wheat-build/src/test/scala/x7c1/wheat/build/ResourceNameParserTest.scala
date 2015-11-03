package x7c1.wheat.build

import org.scalatest.{FlatSpecLike, Matchers}

class ResourceNameParserTest extends FlatSpecLike with Matchers {
   behavior of ResourceNameParser.getClass.getSimpleName

   it can "read prefix from file name" in {
     val Right(prefix) = ResourceNameParser.readPrefix("abcd_ef_ghi.xml")
     prefix.ofClass shouldBe "AbcdEfGhi"
     prefix.ofKey shouldBe "abcd_ef_ghi__"
   }

  it can "read file name with number" in {
    val Right(x0) = ResourceNameParser.readPrefix("abcd0_ef_ghi.xml")
    x0.ofClass shouldBe "Abcd0EfGhi"
    x0.ofKey shouldBe "abcd0_ef_ghi__"

    val Right(x1) = ResourceNameParser.readPrefix("abcd_e0f_ghi.xml")
    x1.ofClass shouldBe "AbcdE0fGhi"
    x1.ofKey shouldBe "abcd_e0f_ghi__"
  }

  it should "fail to invalid file name" in {
     val Left(e0) = ResourceNameParser.readPrefix("0xyz_abcd_ef_ghi.xml")
     e0 shouldBe a[WheatParserError]

     val Left(e1) = ResourceNameParser.readPrefix("xyz__abcd__ef.xml")
     e1 shouldBe a[WheatParserError]

     val Left(e2) = ResourceNameParser.readPrefix("xyz_0abcd.xml")
     e2 shouldBe a[WheatParserError]
   }
 }
