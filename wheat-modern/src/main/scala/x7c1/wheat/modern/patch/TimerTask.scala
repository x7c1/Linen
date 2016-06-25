package x7c1.wheat.modern.patch

object TimerTask {
  def apply[A](f: => A): java.util.TimerTask = new java.util.TimerTask {
    override def run(): Unit = f
  }
}
