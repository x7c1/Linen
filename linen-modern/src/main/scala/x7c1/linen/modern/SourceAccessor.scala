package x7c1.linen.modern

trait SourceAccessor {
  def get: Seq[Source]
}

class SourceStore extends SourceAccessor {

  override def get: Seq[Source] = {
    dummyList
  }

  private lazy val dummyList = (1 to 100) map { n =>
    Source(
      id = n,
      url = s"http://example.com/sample-source-$n",
      title = s"sample-title-$n",
      description = s"sample-description-$n" )
  }

}

