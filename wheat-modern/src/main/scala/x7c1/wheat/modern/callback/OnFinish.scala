package x7c1.wheat.modern.callback

object OnFinish {
  def apply[A](f: => A): OnFinish = {
    new OnFinish {
      override def unwrap[B]: B => Unit = _ => f
    }
  }
  def nop[A]: A => Unit = _ => ()
}

trait OnFinish {
  def unwrap[A]: A => Unit
  def evaluate(): Unit = unwrap[Unit]({})
}
