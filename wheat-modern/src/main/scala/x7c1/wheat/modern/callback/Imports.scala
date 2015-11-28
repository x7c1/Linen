package x7c1.wheat.modern.callback


object Imports {
  implicit object TaskBuilder extends TaskBuilder[OnFinish => Unit, Unit]{
    override def build(f: OnFinish => Unit): CallbackTask[Unit] =
      CallbackTask { g =>
        val done = OnFinish(g{()})
        f(done)
      }
  }
  implicit object TaskBuilder2 extends TaskBuilder[OnFinish => CallbackTask[Unit], Unit]{
    override def build(f: OnFinish => CallbackTask[Unit]): CallbackTask[Unit] =
      CallbackTask { g =>
        val done = OnFinish(g{()})
        f(done).execute()
      }
  }
}
