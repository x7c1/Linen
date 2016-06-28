package x7c1.linen.modern.init.unread

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout._
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.display.unread.{DetailArea, OutlineArea, PaneContainer, SourceArea}
import x7c1.linen.modern.init.unread.entry.{DetailListProviders, OutlineListProviders}
import x7c1.linen.modern.init.unread.source.SourceListProviders
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.account.setup.ClientAccountSetup
import x7c1.linen.repository.loader.crawling.CrawlerFate
import x7c1.linen.repository.preset.Tech
import x7c1.linen.repository.source.unread.RawSourceAccessor
import x7c1.linen.repository.unread._
import x7c1.linen.scene.loader.crawling.SchedulerService
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.resource.MetricsConverter

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

    CrawlerFate run entryMarker.markAsRead() atLeft { e =>
      Log error e.detail
    }
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
      SchedulerService(activity) setupSchedule account.accountId
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
    source = loader.sources,
    entryOutline = loader.outlines,
    entryDetail = loader.details,
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


