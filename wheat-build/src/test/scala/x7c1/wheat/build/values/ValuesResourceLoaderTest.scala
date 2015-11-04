package x7c1.wheat.build.values

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.build.{ParsedResourceElement, SampleLocations}

class ValuesResourceLoaderTest extends FlatSpecLike with Matchers {

  def locations = SampleLocations.values

  def loader = new ValuesResourceLoader(locations.valuesSrc)

  behavior of classOf[ValuesResourceLoader].getName

  it can "create prefix" in {
    val Right(resource) = loader.load("comment.xml")
    resource.prefix.ofClass shouldBe "Comment"
    resource.prefix.ofKey shouldBe "comment__"
    resource.prefix.raw shouldBe "comment"
  }
  it can "create elements" in {
    val Right(resource) = loader.load("comment.xml")
    val e1 = resource.elements.find(_.key == "comment__name_clicked")
    e1 shouldBe Some(ParsedResourceElement(
      key = "comment__name_clicked",
      label = "nameClicked",
      tag = "string"
    ))
    val e2 = resource.elements.find(_.key == "comment__is_experiment")
    e2 shouldBe Some(ParsedResourceElement(
      key = "comment__is_experiment",
      label = "isExperiment",
      tag = "bool"
    ))
  }
}


