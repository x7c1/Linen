package x7c1.wheat.build

import org.scalatest.{Matchers, FlatSpecLike}

class LayoutGeneratorTest extends FlatSpecLike with Matchers {

  def loader = new ResourceLoader(LayoutGenerator.layoutDir)

  behavior of LayoutGenerator.getClass.getName

  it can "inspect resource XML" in {
    val Right(layout) = loader load "comment_row.xml"
    layout.prefix.ofClass shouldBe "CommentRow"

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
    val Right(layout) = loader load "comment_row.xml"
    val sources = LayoutGenerator.applyTemplate(layout)
    val source = sources.head.code

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

class ResourceNameParserTest extends FlatSpecLike with Matchers {
  behavior of "LayoutNameParser"

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
