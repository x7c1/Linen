package x7c1.wheat.modern.callback


object TaskProvider {

  def apply[A](f: => A): CallbackTask[A] = CallbackTask(_(f))

  def of[A, B](f: A)(implicit builder: TaskBuilder[A, B]): CallbackTask[B] = builder build f
}

trait TaskBuilder[A, B]{
  def build(f: A): CallbackTask[B]
}

object Imports {
  implicit object TaskBuilder1 extends TaskBuilder[OnFinish => Unit, Unit]{
    override def build(f: OnFinish => Unit): CallbackTask[Unit] =
      CallbackTask { g =>
        val done = OnFinish(g{()})
        f(done)
      }
  }

  implicit def toTaskBuilder2[A] = new TaskBuilder[OnFinish => CallbackTask[A], Unit]{
    override def build(f: OnFinish => CallbackTask[A]): CallbackTask[Unit] =
      CallbackTask { g =>
        val done = OnFinish(g{()})
        f(done).execute()
      }
  }

  implicit def toTaskBuilder3[A]: TaskBuilder[(A => Unit) => Unit, A] =
    new TaskBuilder[(A => Unit) => Unit, A]{
      override def build(f: (A => Unit) => Unit) = CallbackTask(f)
    }

}
