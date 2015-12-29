package x7c1.linen.modern.init

import java.lang.Math.max

import android.app.Activity
import android.graphics.Point
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import x7c1.linen.glue.res.layout.{EntryDetailRow, EntryRow, MainLayout, SourceRow}
import x7c1.linen.modern.accessor.{EntryAccessor, LinenOpenHelper, SourceAccessor}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class ContainerInitializer(
  activity: Activity,
  override val layout: MainLayout,
  override val sourceRowProvider: ViewHolderProvider[SourceRow],
  override val entryRowProvider: ViewHolderProvider[EntryRow],
  override val entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]
) extends ActionsInitializer
  with SourceAreaInitializer
  with EntryAreaInitializer
  with EntryDetailAreaInitializer {

  def setup(): Unit = {
    updateWidth(1.0, layout.entryDetailArea)

    DummyFactory.setup(layout, activity)

    setupSourceArea()
    setupEntryArea()
    setupEntryDetailArea()

    loader.startLoading()
  }
  def close(): Unit = {
    loader.close()
    database.close()
  }
  private lazy val database =
    new LinenOpenHelper(activity).getReadableDatabase

  private lazy val loader =
    new AccessorLoader(database, layout, activity.getLoaderManager)

  override lazy val accessors = new Accessors(
    source = loader.createSourceAccessor,
    entryOutline = loader.createOutlineAccessor,
    entryDetail = loader.createDetailAccessor
  )
  override lazy val actions = setupActions()

  override lazy val displaySize: Point = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
  override def dipToPixel(dip: Int) = {
    val metrics = activity.getResources.getDisplayMetrics
    TypedValue.applyDimension(COMPLEX_UNIT_DIP, dip, metrics).toInt
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

