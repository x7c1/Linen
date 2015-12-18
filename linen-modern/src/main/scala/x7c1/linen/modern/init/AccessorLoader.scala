package x7c1.linen.modern.init

import android.content.Context
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, Source}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync.async
import x7c1.wheat.modern.decorator.Imports._

import scala.collection.mutable.ListBuffer
import scala.concurrent.SyncVar

class AccessorLoader(context: Context, layout: MainLayout){

  lazy val forOutline: Seq[EntryAccessor[EntryOutline]] = outlineAccessors

  lazy val forDetail: Seq[EntryAccessor[EntryDetail]] = detailAccessors

  lazy val forSource: SourceAccessor = new SourceAccessor {
    override def findAt(position: Int): Option[Source] = {
      sourceAccessor.get.flatMap(_.findAt(position))
    }
    override def positionOf(sourceId: Long): Option[Int] = {
      sourceAccessor.get.flatMap(_.positionOf(sourceId))
    }
    override def length: Int = currentSourceLength.get
  }
  private lazy val currentSourceLength: SyncVar[Int] = {
    val value = new SyncVar[Int]()
    value put 0
    value
  }
  private lazy val sourceAccessor: SyncVar[Option[SourceAccessor]] = {
    val value = new SyncVar[Option[SourceAccessor]]()
    value put None
    value
  }
  private lazy val outlineAccessors = {
    ListBuffer[EntryAccessor[EntryOutline]]()
  }
  private lazy val detailAccessors = {
    ListBuffer[EntryAccessor[EntryDetail]]()
  }
  def startLoading() = async( try {
    val accessor = SourceAccessor create context
    val sourceIds = (0 to accessor.length - 1).take(100).
      map(accessor.findAt).flatMap(_.map(_.id))

    sourceAccessor.take()
    sourceAccessor put Some(accessor)

    currentSourceLength.take()
    currentSourceLength put sourceIds.length

    val positions = EntryAccessor.createPositionMap(context, sourceIds)
    val outlines = EntryAccessor.forEntryOutline(context, sourceIds, positions)
    outlineAccessors += outlines

    val details = EntryAccessor.forEntryDetail(context, sourceIds, positions)
    detailAccessors += details

    layout.sourceList runUi { _ =>

      /*
      it should be written like:
        layout.sourceList.getAdapter.notifyItemRangeInserted(0, sourceIds.length)

      but this 'notifyItemRangeInserted' causes following error (and crash)
        java.lang.IndexOutOfBoundsException:
          Inconsistency detected. Invalid view holder adapter positionViewHolder
      */

      layout.sourceList.getAdapter.notifyDataSetChanged()
      layout.entryList.getAdapter.notifyDataSetChanged()
      layout.entryDetailList.getAdapter.notifyDataSetChanged()
    }
  } catch {
    case e: Throwable =>
      Log error (e.getMessage +: e.getStackTrace.take(30) mkString "\n")
  })
}
