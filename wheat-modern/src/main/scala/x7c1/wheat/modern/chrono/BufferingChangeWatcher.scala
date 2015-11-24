package x7c1.wheat.modern.chrono

import java.util.{Timer, TimerTask}

class BufferingChangeWatcher[A](
  getCurrent: () => A,
  onStop: A => Unit,
  delay: Long = 50,
  period: Long = 75 ){

  private var value: Option[A] = None

  private val timer = new Timer()

  private var task: Option[TimerTask] = None

  def touch(): Unit = {
    task foreach { _.cancel() }
    task = Some apply new BufferedTask
    task foreach { timer.schedule(_, delay, period) }
  }

  private class BufferedTask extends TimerTask {
    override def run(): Unit = {
      val current = getCurrent()
      value match {
        case Some(previous) if previous == current =>
          cancel()
          onStop(current)
        case _ =>
      }
      value = Some(current)
    }
  }

}
