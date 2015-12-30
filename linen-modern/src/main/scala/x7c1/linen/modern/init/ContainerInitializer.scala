package x7c1.linen.modern.init

import android.app.Activity
import android.graphics.Point
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import x7c1.linen.glue.res.layout.{MenuRowItem, MenuRowLabel, EntryDetailRow, EntryRow, MainLayout, SourceRow}
import x7c1.linen.modern.accessor.{EntryAccessor, LinenOpenHelper, SourceAccessor}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class ContainerInitializer(
  activity: Activity,
  override val layout: MainLayout,
  override val menuLabelProvider: ViewHolderProvider[MenuRowLabel],
  override val menuItemProvider: ViewHolderProvider[MenuRowItem],
  override val sourceRowProvider: ViewHolderProvider[SourceRow],
  override val entryRowProvider: ViewHolderProvider[EntryRow],
  override val entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]
) extends ActionsInitializer
  with DrawerMenuInitializer
  with SourceAreaInitializer
  with EntryAreaInitializer
  with EntryDetailAreaInitializer {

  def setup(): Unit = {
    DummyFactory.setup(layout, activity)

    setupDrawerMenu()
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

  override lazy val widthWithMargin: Int = {
    val dipToPixel = (dip: Int) => {
      val metrics = activity.getResources.getDisplayMetrics
      TypedValue.applyDimension(COMPLEX_UNIT_DIP, dip, metrics).toInt
    }
    val radius = 20
    val margin = 10
    displaySize.x - dipToPixel(margin + radius)
  }
  override lazy val displaySize: Point = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
}

class Accessors(
  val source: SourceAccessor,
  val entryOutline: EntryAccessor[EntryOutline],
  val entryDetail: EntryAccessor[EntryDetail]
)
