package x7c1.wheat.build.values

import org.scalatest.{Matchers, FlatSpecLike}
import x7c1.wheat.build.SampleLocations

class ValuesInterfacePartsFactoryTest extends FlatSpecLike with Matchers {
  def locations = SampleLocations.values
  def loader = new ValuesResourceLoader(locations.valuesSrc)
  def factory = new ValuesInterfacePartsFactory(locations.packages)

  behavior of factory.getClass.getName

  it can "create ValuesParts" in {
    val Right(resource) = loader.load("comment.xml")
    val parts = factory.createFrom(resource)

    parts.declarePackage shouldBe "package x7c1.linen.glue.res.values;"
    parts.prefix.ofClass shouldBe "Comment"

    parts.methods should include("String nameClicked();")
    parts.methods should include("String contentClicked();")
    parts.methods should include("Boolean isExperiment();")
  }

}

class ValuesProviderPartsTest extends FlatSpecLike with Matchers {
  def locations = SampleLocations.values
  def loader = new ValuesResourceLoader(locations.valuesSrc)
  def factory = new ValuesProviderPartsFactory(locations.packages)

  behavior of factory.getClass.getName

  it can "create methods" in {
    val Right(resource) = loader.load("comment.xml")
    val parts = factory.createFrom(resource)

    parts.methods should
      include("public String nameClicked(){")

    parts.methods should
      include("nameClicked = context.getResources().getString(nameClickedId);")

    parts.methods should
      include("isExperiment = context.getResources().getBoolean(isExperimentId);")
  }

  it can "create fields" in {
    val Right(resource) = loader.load("comment.xml")
    val parts = factory.createFrom(resource)

    parts.fields should include(
      "private final int nameClickedId;")

    parts.fields should include(
      "private String nameClicked;")

    parts.fields should include(
      "private Boolean isExperiment;")
  }

}

class ValuesSourcesFactoryTest extends FlatSpecLike with Matchers {
  def locations = SampleLocations.values
  def loader = new ValuesResourceLoader(locations.valuesSrc)
  def factory = new ValuesSourcesFactory(locations)

  behavior of factory.getClass.getName

  it can "generate java interface source" in {
    val Right(resource) = loader.load("comment.xml")
    val Seq(s1, _*) = factory createFrom resource

    s1.code should include("package x7c1.linen.glue.res.values;")
    s1.code should include("public interface CommentValues")
    s1.code should include("String nameClicked();")
    s1.code should include("Boolean isExperiment();")

    s1.file.getPath shouldBe
      "linen-glue/src/main/java/x7c1/linen/glue/res/values/CommentValues.java"
  }
  it can "generate java class source" in {
    val Right(resource) = loader.load("comment.xml")
    val Seq(_, s1) = factory createFrom resource

    s1.code should include("package x7c1.linen.res.values;")
    s1.code should include("public class CommentValuesProvider")
    s1.code should include("public String nameClicked(){")
    s1.code should include("public Boolean isExperiment(){")

    s1.file.getPath shouldBe
      "linen-starter/src/main/java/x7c1/linen/res/values/CommentValuesProvider.java"
  }
}
