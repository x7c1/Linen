package x7c1.wheat.modern.chrono

import java.util.{Timer, TimerTask}

class BufferingChangeWatcher[A](
  getCurrent: () => A,
  onStop: A => Unit,
  onError: Throwable => Unit,
  delay: Long,
  period: Long ){

  private var value: Option[A] = None

  private val timer = new Timer()

  private var task: Option[TimerTask] = None

  def touch(): Unit = {
    task foreach { _.cancel() }
    task = Some apply new BufferedTask
    task foreach { timer.schedule(_, delay, period) }
  }

  private class BufferedTask extends TimerTask {
    override def run(): Unit = try {
      val current = getCurrent()
      value match {
        case Some(previous) if previous == current =>
          cancel()
          onStop(current)
        case _ =>
      }
      value = Some(current)
    } catch {
      case e: Throwable => onError(e)
    }
  }

}
