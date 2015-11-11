package x7c1.linen.modern

trait SourceItemAccessor {
  def get: Seq[SourceItem]
}

class SourceItemStore extends SourceItemAccessor {
  override def get: Seq[SourceItem] = dummyList

  private lazy val dummyList = (1 to 100) map { n =>
    SourceItem(
      id = n,
      url = s"http://example.com/sample-source-$n",
      title = s"sample-title-$n",
      content = s"sample content $n",
      createdAt = LinenDate.dummy()
    )
  }
}
