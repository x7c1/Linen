package x7c1.wheat.modern.callback


object Imports {
  implicit object TaskBuilder extends TaskBuilder[OnFinish => Unit, Unit]{
    override def build(f: OnFinish => Unit): CallbackTask[Unit] = CallbackTask { g =>
      val done = OnFinish(g{()})
      f(done)
    }
  }
}
