package x7c1.linen.modern.init

import android.app.Activity
import android.graphics.Point
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.KeyEvent
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{MenuRowSeparator, EntryDetailRow, EntryRow, MainLayout, MenuRowItem, MenuRowLabel, SourceRow}
import x7c1.linen.modern.accessor.{EntryAccessor, LinenOpenHelper, RawSourceAccessor, UnreadSourceAccessor}
import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, PaneContainer, SourceArea}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class ContainerInitializer(
  val activity: Activity with ActivityControl,
  val layout: MainLayout,
  val menuLabelProvider: ViewHolderProvider[MenuRowLabel],
  val menuItemProvider: ViewHolderProvider[MenuRowItem],
  val menuSeparatorProvider: ViewHolderProvider[MenuRowSeparator],
  val sourceRowProvider: ViewHolderProvider[SourceRow],
  val entryRowProvider: ViewHolderProvider[EntryRow],
  val entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]
) extends ActionsInitializer
  with DrawerMenuInitializer
  with SourceAreaInitializer
  with EntryAreaInitializer
  with EntryDetailAreaInitializer {

  def setup(): Unit = {
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
  def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    keyCode match {
      case KeyEvent.KEYCODE_BACK =>
        actions.drawer.onBack() || actions.container.onBack()
      case _ =>
        false
    }
  }
  private lazy val helper = new LinenOpenHelper(activity)

  private lazy val database = helper.getReadableDatabase

  private lazy val loader =
    new AccessorLoader(database, layout, activity.getLoaderManager)

  lazy val container = new PaneContainer(
    view = layout.paneContainer,
    displayWidth = displaySize.x,
    sourceArea = new SourceArea(
      sources = accessors.source,
      recyclerView = layout.sourceList,
      getPosition = () => 0
    ),
    entryArea = new EntryArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => {
        layout.sourceArea.getWidth
      }
    ),
    entryDetailArea = new EntryDetailArea(
      toolbar = layout.entryDetailToolbar,
      recyclerView = layout.entryDetailList,
      getPosition = () => {
        layout.sourceArea.getWidth + layout.entryArea.getWidth
      }
    )
  )
  lazy val accessors = new Accessors(
    source = loader.createSourceAccessor,
    entryOutline = loader.createOutlineAccessor,
    entryDetail = loader.createDetailAccessor,
    rawSource = new RawSourceAccessor(helper)
  )
  lazy val actions = setupActions()

  def dipToPixel(dip: Int): Int = {
    val metrics = activity.getResources.getDisplayMetrics
    TypedValue.applyDimension(COMPLEX_UNIT_DIP, dip, metrics).toInt
  }
  lazy val widthWithMargin: Int = {
    val radius = 20
    val margin = 10
    displaySize.x - dipToPixel(margin + radius)
  }
  lazy val displaySize: Point = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
}

class Accessors(
  val source: UnreadSourceAccessor,
  val entryOutline: EntryAccessor[EntryOutline],
  val entryDetail: EntryAccessor[EntryDetail],
  val rawSource: RawSourceAccessor
)
