package x7c1.wheat.modern.kinds.callback

import x7c1.wheat.modern.kinds.{CallbackTask, OnFinish}

object Imports {
  implicit object TaskBuilder extends TaskBuilder[OnFinish => Unit, Unit]{
    override def build(f: OnFinish => Unit): CallbackTask[Unit] = CallbackTask { g =>
      val done = OnFinish(g{()})
      f(done)
    }
  }
}
