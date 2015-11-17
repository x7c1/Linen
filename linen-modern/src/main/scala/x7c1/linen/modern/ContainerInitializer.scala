package x7c1.linen.modern

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import x7c1.linen.glue.res.layout.{SourceRow, ActivityMain}
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
  private lazy val container = new PaneContainer(
    layout.swipeContainer,
    new SourcesArea(layout.sampleLeftList, 0),
    new EntriesArea(layout.sampleCenterList, 864)
  )
  private def updateWidth(ratio: Double, view: View): Unit = {
    val params = view.getLayoutParams
    params.width = (ratio * displaySize.x).toInt
    view setLayoutParams params
  }
}
