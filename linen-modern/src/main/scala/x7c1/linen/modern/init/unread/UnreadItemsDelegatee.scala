package x7c1.linen.modern.init.unread

import android.app.Activity
import android.graphics.Point
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.KeyEvent
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{MainLayout, MenuRowLabel, MenuRowSeparator, MenuRowTitle, UnreadDetailRowEntry, UnreadDetailRowSource, UnreadOutlineRowEntry, UnreadOutlineRowSource, UnreadSourceRow}
import x7c1.linen.modern.accessor.preset.{PresetAccount, ClientAccount, ClientAccountSetup}
import x7c1.linen.modern.accessor.{EntryAccessor, LinenOpenHelper, RawSourceAccessor, UnreadSourceAccessor}
import x7c1.linen.modern.display.unread.{DetailArea, OutlineArea, PaneContainer, SourceArea}
import x7c1.linen.modern.struct.{UnreadDetail, UnreadOutline}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log

class UnreadItemsDelegatee(
  val activity: Activity with ActivityControl,
  val layout: MainLayout,
  val menuRowProviders: MenuRowProviders,
  val unreadRowProviders: UnreadRowProviders
) extends ActionsInitializer
  with DrawerMenuInitializer
  with SourceAreaInitializer
  with OutlineAreaInitializer
  with DetailAreaInitializer {

  def setup(): Unit = {
    setupDrawerMenu()
    setupSourceArea()
    setupEntryArea()
    setupEntryDetailArea()

    clientAccount match {
      case Some(account) => loader.startLoading(account)
      case None => Log error s"account not found"
    }
  }
  def close(): Unit = {
    Log info s"[start]"
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

    // using preset account temporally to display channels
    val tmp = helper.readable.find[PresetAccount]() match {
      case Left(error) => None
      case Right(None) => None
      case Right(Some(account)) => Some(ClientAccount(account.accountId))
    }

    val account = ClientAccountSetup(helper).findOrCreate() match {
      case Left(error) =>
        Log error error.toString
        None
      case Right(account) =>
        Some(account)
    }
    tmp orElse account
  }
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
  val forSource: ViewHolderProvider[UnreadSourceRow],
  val forOutlineSource: ViewHolderProvider[UnreadOutlineRowSource],
  val forOutlineEntry: ViewHolderProvider[UnreadOutlineRowEntry],
  val forDetailSource: ViewHolderProvider[UnreadDetailRowSource],
  val forDetailEntry: ViewHolderProvider[UnreadDetailRowEntry]
)
