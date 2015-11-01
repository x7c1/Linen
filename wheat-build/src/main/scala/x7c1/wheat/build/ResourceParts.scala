package x7c1.wheat.build


trait ResourceParts {
  def prefix: ResourcePrefix
}

trait ResourcePartsFactory [A <: ResourceParts]{
  def createFrom(layout: ParsedResource): A
}

case class ResourcePrefix(
  raw: String,
  ofClass: String,
  ofKey: String
)

case class ParsedResource(
  prefix: ResourcePrefix,
  elements: Seq[ParsedResourceElement]
)

case class ParsedResourceElement(
  key: String,
  label: String,
  tag: String
)
