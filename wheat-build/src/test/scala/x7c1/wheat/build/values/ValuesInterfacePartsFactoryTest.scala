package x7c1.wheat.build.values

import org.scalatest.{Matchers, FlatSpecLike}
import x7c1.wheat.build.SampleLocations

class ValuesInterfacePartsFactoryTest extends FlatSpecLike with Matchers {
  def locations = SampleLocations.values
  def loader = new ValuesResourceLoader(locations.valuesSrc)
  def factory = new ValuesInterfacePartsFactory(locations.packages)

  behavior of factory.getClass.getName

  it can "create ValuesParts with methods" in {
    val Right(resource) = loader.load("comment.xml")
    val parts = factory.createFrom(resource)

    parts.declarePackage shouldBe "package x7c1.linen.glue.res.values;"
    parts.prefix.ofClass shouldBe "Comment"

    parts.methods should include("String nameClicked();")
    parts.methods should include("String contentClicked();")
    parts.methods should include("boolean isExperiment();")
  }

}
