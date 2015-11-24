package x7c1.wheat.modern.callback


object TaskProvider {

  def apply[A](f: => A): CallbackTask[A] = CallbackTask(_(f))

  def of[A, B](f: A)(implicit builder: TaskBuilder[A, B]): CallbackTask[B] = builder build f
}

trait TaskBuilder[A, B]{
  def build(f: A): CallbackTask[B]
}
