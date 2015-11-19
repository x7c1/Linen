package x7c1.wheat.modern.chrono

import java.util.{Timer, TimerTask}

class BufferingTimer(delay: Int){
  private val timer = new Timer()
  private var task: Option[TimerTask] = None

  def touch[A](f: => A) = {
    task foreach { _.cancel() }
    task = Some apply new BufferedTask(f)
    task foreach { timer.schedule(_, delay) }
  }
  private class BufferedTask[A](f: => A) extends TimerTask {
    override def run(): Unit = f
  }
}
