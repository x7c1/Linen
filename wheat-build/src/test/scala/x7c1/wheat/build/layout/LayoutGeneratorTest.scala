package x7c1.wheat.build.layout

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.build.SampleLocations

class LayoutGeneratorTest extends FlatSpecLike with Matchers {

  def loader = new LayoutResourceLoader(locations.layoutSrc)

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

  def locations = SampleLocations.layout

  it can "generate java source" in {
    val Right(layout) = loader load "comment_row.xml"
    val sources = new LayoutSourcesFactory(locations).createFrom(layout)
    val source = sources.head.code

    source should include("import android.view.View;")
    source should include("import android.widget.TextView;")

    source should include("public final TextView content;")
    source should include("this.content = content;")
  }
}
