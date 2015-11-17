package x7c1.linen.modern

import java.lang.Math.max

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import x7c1.linen.glue.res.layout.{ActivityMain, SourceRow}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class ContainerInitializer(
  activity: Activity,
  layout: ActivityMain,
  sourceRowProvider: ViewHolderProvider[SourceRow]) {

  def setup(): Unit = {
    updateWidth(0.9, layout.swipeLayoutLeft)
    updateWidth(0.8, layout.swipeLayoutCenter)
    updateWidth(0.9, layout.swipeLayoutRight)

    layout.sampleLeftList setLayoutManager new LinearLayoutManager(activity)
    layout.sampleLeftList setAdapter adapter

    layout.sampleCenterList setLayoutManager new LinearLayoutManager(activity)
  }
  private lazy val displaySize = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
  private lazy val adapter = new SourceRowAdapter(
    new SourceStore(),
    new SourceSelectObserver(container),
    sourceRowProvider
  )
  private lazy val container = {
    val length = layout.swipeContainer.getChildCount
    val children = 0 to (length - 1) map layout.swipeContainer.getChildAt
    val positions = new PanePositions(children, displaySize.x)

    new PaneContainer(
      layout.swipeContainer,
      new SourcesArea(
        recyclerView = layout.sampleLeftList,
        getPosition = () => positions of layout.swipeLayoutLeft
      ),
      new EntriesArea(
        recyclerView = layout.sampleCenterList,
        getPosition = () => positions of layout.swipeLayoutCenter
      )
    )
  }
  private def updateWidth(ratio: Double, view: View): Unit = {
    val params = view.getLayoutParams
    params.width = (ratio * displaySize.x).toInt
    view setLayoutParams params
  }
}

private class PanePositions(children: Seq[View], displayWidth: Int){
  def of(view: View): Int = {
    positions find (_._1 == view) map (_._2) getOrElse {
      throw new IllegalStateException("view not found")
    }
  }
  private lazy val positions = {
    val xs = children.scanLeft(0){_ + _.getWidth}
    children.zip(xs).zipWithIndex map { case ((view, start), i) =>
      val position =
        if (i == children.length - 1)
          start - (displayWidth - view.getWidth)
        else
          start - (displayWidth - view.getWidth) / 2

      view -> max(0, position)
    }
  }
}
