package x7c1.wheat.build

import org.scalatest.{Matchers, FlatSpecLike}

class LayoutGeneratorTest extends FlatSpecLike with Matchers {

  behavior of LayoutGenerator.getClass.getName

  it can "inspect resource XML" in {
    val Right(layout) = LayoutGenerator inspect "comment_row.xml"
    layout.prefix shouldBe "CommentRow"

    val elements = layout.elements

    val Some(name) = elements.find(_.key == "comment_row__name")
    name.key shouldBe "comment_row__name"
    name.label shouldBe "name"
    name.tag shouldBe "TextView"

    val Some(content) = elements.find(_.key == "comment_row__content")
    content.key shouldBe "comment_row__content"
    content.label shouldBe "content"
    content.tag shouldBe "TextView"
  }

  it can "generate java source" in {
    val Right(layout) = LayoutGenerator inspect "comment_row.xml"
    val source = LayoutGenerator.applyTemplate(layout)

    source should include("import android.view.View;")
    source should include("import android.widget.TextView;")

    source should include("public final TextView content;")
    source should include("this.content = content;")
  }
}

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

class LayoutNameParserTest extends FlatSpecLike with Matchers {
  behavior of "LayoutNameParser"

  it can "read prefix from file name" in {
    val Right(prefix) = LayoutNameParser.readPrefix("abcd_ef_ghi.xml")
    prefix.camel shouldBe "AbcdEfGhi"
    prefix.key shouldBe "abcd_ef_ghi__"
  }

  it should "fail to invalid file name" in {
    val Left(e) = LayoutNameParser.readPrefix("0xyz_abcd_ef_ghi.xml")
    e shouldBe a[WheatParserError]
  }
}
