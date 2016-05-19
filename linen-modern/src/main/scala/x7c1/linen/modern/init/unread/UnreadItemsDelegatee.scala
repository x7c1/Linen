package x7c1.linen.modern.init.unread

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{MenuRowLabel, MenuRowSeparator, MenuRowTitle, UnreadDetailRow, UnreadDetailRowEntry, UnreadDetailRowFooter, UnreadDetailRowSource, UnreadItemsLayout, UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowFooter, UnreadOutlineRowSource, UnreadSourceRow, UnreadSourceRowFooter, UnreadSourceRowItem}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.display.unread.{DetailArea, OutlineArea, PaneContainer, SourceArea}
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.account.setup.ClientAccountSetup
import x7c1.linen.repository.entry.unread.{EntryAccessor, UnreadEntry}
import x7c1.linen.repository.source.unread.{RawSourceAccessor, UnreadSourceAccessor}
import x7c1.linen.repository.unread.{AccessorLoader, Accessors, BrowsedEntriesMarker, EntryKind, FooterKind, SourceKind}
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.resource.{MetricsConverter, ViewHolderProviders}

import scala.concurrent.Future

class UnreadItemsDelegatee(
  val activity: Activity with ActivityControl with ServiceControl,
  val layout: UnreadItemsLayout,
  val menuRowProviders: MenuRowProviders,
  val unreadRowProviders: UnreadRowProviders
) extends ActionsInitializer
  with DrawerMenuInitializer
  with SourceAreaInitializer
  with OutlineAreaInitializer
  with DetailAreaInitializer {

  def setup(): Unit = {
    Log info s"[init]"
    Log info s"db:ver.${database.getVersion}"

    setupSourceArea()
    setupEntryArea()
    setupEntryDetailArea()
    setupDrawerMenu()
    setupLoaderSchedule()
  }
  def onPause(): Unit = {
    Log info s"[init]"

    import x7c1.linen.repository.loader.crawling.Implicits._
    Future { entryMarker.markAsRead() }
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
  private def setupLoaderSchedule(): Unit = {
    clientAccount foreach { account =>
      ServiceCaller.using[UpdaterMethods].
        startService(activity, activity getClassOf Updater){
          _ setupLoaderSchedule account.accountId
        }
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
  protected lazy val entryMarker = BrowsedEntriesMarker(helper, accessors)

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
        Log error error.detail
        None
      case Right(account) =>
        Some(account)
    }
  }
  def footerHeightOf(recyclerView: RecyclerView) = {
    recyclerView.getHeight - converter.dipToPixel(10)
  }
  lazy val converter = MetricsConverter(activity)

  lazy val widthWithMargin: Int = {
    val radius = 20
    val margin = 10
    displaySize.x - converter.dipToPixel(margin + radius)
  }
  lazy val displaySize: Point = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
}



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
