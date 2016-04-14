package x7c1.linen.modern.init.unread

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.KeyEvent
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{MenuRowLabel, MenuRowSeparator, MenuRowTitle, UnreadDetailRow, UnreadDetailRowEntry, UnreadDetailRowFooter, UnreadDetailRowSource, UnreadItemsLayout, UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowFooter, UnreadOutlineRowSource, UnreadSourceRow, UnreadSourceRowFooter, UnreadSourceRowItem}
import x7c1.linen.modern.display.unread.{DetailArea, OutlineArea, PaneContainer, SourceArea}
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.account.setup.ClientAccountSetup
import x7c1.linen.repository.entry.unread.{EntryAccessor, UnreadDetail, UnreadEntry, UnreadOutline}
import x7c1.linen.repository.source.unread.{RawSourceAccessor, UnreadSourceAccessor}
import x7c1.linen.repository.unread.{AccessorLoader, EntryKind, FooterKind, SourceKind}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.resource.ViewHolderProviders

class UnreadItemsDelegatee(
  val activity: Activity with ActivityControl,
  val layout: UnreadItemsLayout,
  val menuRowProviders: MenuRowProviders,
  val unreadRowProviders: UnreadRowProviders
) extends ActionsInitializer
  with DrawerMenuInitializer
  with SourceAreaInitializer
  with OutlineAreaInitializer
  with DetailAreaInitializer {

  def setup(): Unit = {
    setupSourceArea()
    setupEntryArea()
    setupEntryDetailArea()
    setupDrawerMenu()
  }
  def close(): Unit = {
    Log info s"[start]"
    closeDrawerMenu()
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
  protected lazy val helper = new DatabaseHelper(activity)

  protected lazy val reader = new UnreadChannelsReader(
    client = clientAccount,
    loader = loader,
    onLoaded = new OnAccessorsLoadedListener(
      layout = layout,
      container = container,
      pointer = new SourcePointer(accessors.source, container, actions),
      drawer = actions.drawer
    )
  )
  private lazy val database = helper.getReadableDatabase

  private lazy val loader = AccessorLoader(database, activity)

  lazy val container = new PaneContainer(
    view = layout.paneContainer,
    displayWidth = displaySize.x,
    sourceArea = new SourceArea(
      sources = accessors.source,
      recyclerView = layout.sourceList,
      getPosition = () => 0
    ),
    outlineArea = new OutlineArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => {
        layout.sourceArea.getWidth
      }
    ),
    detailArea = new DetailArea(
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

  lazy val clientAccount: Option[ClientAccount] = setupClientAccount()

  def setupClientAccount(): Option[ClientAccount] = {
    ClientAccountSetup(helper).findOrCreate() match {
      case Left(error) =>
        Log error error.toString
        None
      case Right(account) =>
        Some(account)
    }
  }
  def dipToPixel(dip: Int): Int = {
    val metrics = activity.getResources.getDisplayMetrics
    TypedValue.applyDimension(COMPLEX_UNIT_DIP, dip, metrics).toInt
  }
  def footerHeightOf(recyclerView: RecyclerView) = {
    recyclerView.getHeight - dipToPixel(10)
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
  val entryOutline: EntryAccessor[UnreadOutline],
  val entryDetail: EntryAccessor[UnreadDetail],
  val rawSource: RawSourceAccessor
)

class MenuRowProviders(
  val forTitle: ViewHolderProvider[MenuRowTitle],
  val forLabel: ViewHolderProvider[MenuRowLabel],
  val forSeparator: ViewHolderProvider[MenuRowSeparator]
)

class UnreadRowProviders(
  val forSourceArea: SourceListProviders,
  val forOutlineArea: OutlineListProviders,
  val forDetailArea: DetailListProviders
)

class SourceListProviders(
  val forItem: ViewHolderProvider[UnreadSourceRowItem],
  val forFooter: ViewHolderProvider[UnreadSourceRowFooter]
) extends ViewHolderProviders[UnreadSourceRow] {

  override protected val all = Seq(
    forItem,
    forFooter
  )
  def createViewTyper(accessor: UnreadSourceAccessor): Int => Int = {
    position =>
      val provider = if (position == accessor.length - 1){
        forFooter
      } else {
        forItem
      }
      provider.layoutId
  }
}

trait EntryRowProviders{
  self: ViewHolderProviders[_] =>

  def forSource: ViewHolderProvider[_]
  def forEntry: ViewHolderProvider[_]
  def forFooter: ViewHolderProvider[_]

  def createViewTyper[A <: UnreadEntry](accessor: EntryAccessor[A]): Int => Int = {
    val map = accessor createPositionMap {
      case SourceKind => forSource
      case EntryKind => forEntry
      case FooterKind => forFooter
    }
    position => map(position).layoutId
  }
}

class OutlineListProviders(
  val forSource: ViewHolderProvider[UnreadOutlineRowSource],
  val forEntry: ViewHolderProvider[UnreadOutlineRowEntry],
  val forFooter: ViewHolderProvider[UnreadOutlineRowFooter]
) extends ViewHolderProviders[UnreadOutlineRow] with EntryRowProviders {

  override protected val all = Seq(
    forSource,
    forEntry,
    forFooter
  )
}

class DetailListProviders(
  val forSource: ViewHolderProvider[UnreadDetailRowSource],
  val forEntry: ViewHolderProvider[UnreadDetailRowEntry],
  val forFooter: ViewHolderProvider[UnreadDetailRowFooter]
) extends ViewHolderProviders[UnreadDetailRow] with EntryRowProviders {

  override protected val all = Seq(
    forSource,
    forEntry,
    forFooter
  )
}
