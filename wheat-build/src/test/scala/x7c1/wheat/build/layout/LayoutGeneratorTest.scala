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

  it can "expand <include> tag" in {
    val Right(resource) = loader load "main_layout.xml"
    val elements = resource.elements

    val Some(x1) = elements.find(_.key == "main_layout__menu_area")
    x1.label shouldBe "menuArea"
    x1.tag shouldBe "LinearLayout"

    val Some(x2) = elements.find(_.key == "activity_main__source_toolbar")
    x2.label shouldBe "sourceToolbar"
    x2.tag shouldBe "android.support.v7.widget.Toolbar"
  }

  behavior of classOf[ViewHolderSourcesFactory].getSimpleName

  it can "generate files around ViewHolder" in {
    val Right(layout) = loader load "main_layout.xml"
    val sources = new ViewHolderSourcesFactory(locations).createFrom(layout)

    val Some(source1) = sources.find(_.file.getName.endsWith("MainLayoutProvider.java"))
    source1.code should
      include("class MainLayoutProvider implements ViewHolderProvider<MainLayout>")
    source1.code should
      include("public MainLayout inflate(ViewGroup parent, boolean attachToRoot)")

    val Some(source2) = sources.find(_.file.getName.endsWith("MainLayout.java"))
    source2.code should
      include("class MainLayout extends RecyclerView.ViewHolder")
  }

  it can "generate parent ViewHolder" in {
    val Right(layout) = loader load "menu_row__label.xml"
    val sources = new ViewHolderSourcesFactory(locations).createFrom(layout)
    val Some(source) = sources.find(_.file.getName.endsWith("MenuRow.java"))
    source.code should include("public class MenuRow extends RecyclerView.ViewHolder")
  }

}
