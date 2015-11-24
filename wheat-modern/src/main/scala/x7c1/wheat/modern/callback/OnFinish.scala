package x7c1.wheat.modern.callback

object OnFinish {
  def apply[A](f: => A): OnFinish = {
    new OnFinish {
      override def by[B]: B => Unit = _ => f
    }
  }
}

trait OnFinish {
  def by[A]: A => Unit
  def evalulate(): Unit = by[Unit]({})
}
