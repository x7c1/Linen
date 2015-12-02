package x7c1.linen.modern.accessor

import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.OnFinish
import x7c1.wheat.modern.callback.Imports._

trait OnEntryInsertedListener { self =>

  def onInserted(event: EntryInsertedEvent)(done: OnFinish): Unit

  def append(listener: OnEntryInsertedListener): OnEntryInsertedListener =
    new OnEntryInsertedListener {
      override def onInserted(event: EntryInsertedEvent)(done: OnFinish): Unit = {
        val f = for {
          _ <- task of self.onInserted(event) _
          _ <- task of listener.onInserted(event) _
        } yield {
          done.evaluate()
        }
        f.execute()
      }
    }
}

case class EntryInsertedEvent(
  position: Int,
  length: Int
)
