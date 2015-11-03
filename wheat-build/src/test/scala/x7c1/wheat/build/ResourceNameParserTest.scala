package x7c1.wheat.build

import org.scalatest.{FlatSpecLike, Matchers}

class ResourceNameParserTest extends FlatSpecLike with Matchers {
   behavior of ResourceNameParser.getClass.getSimpleName

   it can "read prefix from file name" in {
     val Right(prefix) = ResourceNameParser.readPrefix("abcd_ef_ghi.xml")
     prefix.ofClass shouldBe "AbcdEfGhi"
     prefix.ofKey shouldBe "abcd_ef_ghi__"
   }

   it should "fail to invalid file name" in {
     val Left(e) = ResourceNameParser.readPrefix("0xyz_abcd_ef_ghi.xml")
     e shouldBe a[WheatParserError]
   }
 }
