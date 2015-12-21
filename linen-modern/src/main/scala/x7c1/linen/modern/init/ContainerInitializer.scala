package x7c1.linen.modern.init

import java.lang.Math.max

import android.app.Activity
import android.graphics.Point
import android.view.View
import x7c1.linen.glue.res.layout.{EntryDetailRow, EntryRow, MainLayout, SourceRow}
import x7c1.linen.modern.accessor.{EntryAccessor, EntryAccessorHolder, LinenOpenHelper, SourceAccessor, SourceAccessorHolder}
import x7c1.linen.modern.action.{Actions, ContainerAction, EntryAreaAction, EntryDetailAreaAction, SourceAreaAction}
import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, PaneContainer, SourceArea}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class ContainerInitializer(
  activity: Activity,
  override val layout: MainLayout,
  override val sourceRowProvider: ViewHolderProvider[SourceRow],
  override val entryRowProvider: ViewHolderProvider[EntryRow],
  override val entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]
) extends SourceAreaInitializer
  with EntryAreaInitializer
  with EntryDetailAreaInitializer {

  def setup(): Unit = {
    updateWidth(0.85, layout.sourceArea)
    updateWidth(0.9, layout.entryArea)
    updateWidth(0.95, layout.entryDetailArea)

    DummyFactory.setup(layout, activity)

    setupSourceArea()
    setupEntryArea()
    setupEntryDetailArea()

    loader.startLoading()
  }
  def close(): Unit = {
    database.close()
  }
  private lazy val database = {
    new LinenOpenHelper(activity).getReadableDatabase
  }
  private lazy val loader = {
    new AccessorLoader(database, layout)
  }
  override lazy val accessors = {
    new Accessors(
      source = new SourceAccessorHolder(loader.forSource),
      entryOutline = new EntryAccessorHolder[EntryOutline](loader.forOutline),
      entryDetail = new EntryAccessorHolder[EntryDetail](loader.forDetail)
    )
  }
  private lazy val displaySize: Point = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
  override lazy val actions = {
    val panePosition = {
      val length = layout.paneContainer.getChildCount
      val children = 0 to (length - 1) map layout.paneContainer.getChildAt
      new PanePosition(children, displaySize.x)
    }
    val sourceArea = new SourceArea(
      sources = accessors.source,
      recyclerView = layout.sourceList,
      getPosition = () => panePosition of layout.sourceArea
    )
    val entryArea = new EntryArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => panePosition of layout.entryArea
    )
    val entryDetailArea = new EntryDetailArea(
      toolbar = layout.entryDetailToolbar,
      recyclerView = layout.entryDetailList,
      getPosition = () => panePosition of layout.entryDetailArea
    )
    new Actions(
      new ContainerAction(
        container = new PaneContainer(layout.paneContainer),
        entryArea,
        entryDetailArea
      ),
      new SourceAreaAction(sourceArea, accessors.source),
      new EntryAreaAction(
        entryArea = entryArea,
        sourceAccessor = accessors.source,
        entryAccessor = accessors.entryOutline
      ),
      new EntryDetailAreaAction(entryDetailArea, accessors.entryDetail)
    )
  }
  private def updateWidth(ratio: Double, view: View): Unit = {
    val params = view.getLayoutParams
    params.width = (ratio * displaySize.x).toInt
    view setLayoutParams params
  }
}

class Accessors(
  val source: SourceAccessor,
  val entryOutline: EntryAccessor[EntryOutline],
  val entryDetail: EntryAccessor[EntryDetail]
)

private class PanePosition(children: Seq[View], displayWidth: Int){
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
