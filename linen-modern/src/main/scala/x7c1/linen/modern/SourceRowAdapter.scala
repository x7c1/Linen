package x7c1.linen.modern

//import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import android.widget.TextView
import x7c1.linen.glue.res.layout.SourceRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scalaz.concurrent.Task
import scalaz.{\/-, -\/}


class SourceRowAdapter(
  sourceAccessor: SourceAccessor,
  sourceSelectedListener: OnSourceSelectedListener,
  viewHolderProvider: ViewHolderProvider[SourceRow]) extends Adapter[SourceRow]{

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }

  override def onBindViewHolder(holder: SourceRow, position: Int) = {
    val source = sourceAccessor.get(position)
    holder.title.text = source.title
    holder.description.text = source.description
    holder.itemView onClick { view =>
      val event = SourceSelectedEvent(source, position)
      sourceSelectedListener onSourceSelected event
    }
  }

  override def getItemCount = sourceAccessor.get.length
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

class PaneController(sampleText: TextView) extends OnSourceSelectedListener {

  def container = new Container

  def entriesArea = new EntriesArea

  def sourcesArea = new SourcesArea

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info event

    val task1 = for {
      _ <- taskOf(container focusOn entriesArea)
      _ <- taskOf(sourcesArea scrollTo event.position)
    } yield {
      Log info s"[done] source selected $event"
    }
    val task2 = for {
      _ <- taskOf(entriesArea displayOrLoad event.sourceId)
    } yield {
      Log info s"[done] entries loaded ${event.sourceId}"
    }
    Seq(task1, task2) foreach runAsync
  }

  def runAsync[A](task: CallbackTask[A]) = {
    Task(task()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }

}

class Container {
  def focusOn(pane: Pane)(onFinish: ContainerFocusedEvent => Unit): Unit = {
    Log info "start"

    onFinish(new ContainerFocusedEvent)
  }
}

class ContainerFocusedEvent

class ScrollObserver (recyclerView: RecyclerView){

  def init() = {
    recyclerView onScroll { event =>
    }
  }

}
