package x7c1.linen.repository.channel.order

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{ChannelRankParts, HasAccountId, HasChannelStatusKey}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.WritableDatabase.transaction
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.observer.recycler.order.{OrderUpdater, PositionedItems}

class ChannelOrderUpdater[A: HasChannelRank] private (
  db: SQLiteDatabase) extends OrderUpdater[A]{

  private val extract = implicitly[HasChannelRank[A]]

  override def update(items: PositionedItems[A]) = {
    val either = transaction(db){ writable =>
      writable update ChannelRankParts(
        origin = extract toId items.current,
        channelRank = calculateRank(items)
      )
    }
    either.left foreach {
      e => Log error format(e){"[failed]"}
    }
  }
  def normalizeRanksOf[X: HasAccountId](account: X): Either[SQLException, Int] = {
    for {
      channels <- db.selectorOf[OrderedChannel].collectFrom(account).right
      affected <- {
        val ranks = channels.zipWithIndex map {
          case (channel, rank) => ChannelRankParts(channel, rank)
        }
        transaction(db)(_ updateAll ranks).right
      }
    } yield affected
  }
  def updateDefaultRanks[X: HasAccountId](account: X): Either[SQLException, Int] = {
    for {
      channels <- db.selectorOf[DefaultRankChannel].collectFrom(account).right
      affected <- {
        val ranks = channels.zipWithIndex.reverse map {
          case (channel, i) => ChannelRankParts(channel, -i)
        }
        transaction(db)(_ updateAll ranks).right
      }
    } yield affected
  }
  private def calculateRank(items: PositionedItems[A]): Double = {
    (items.previous, items.next) match {
      case (None, Some(next)) =>
        (extract rankOf next) - 1
      case (Some(previous), Some(next)) =>
        ((extract rankOf previous) + (extract rankOf next)) / 2
      case (Some(previous), None) =>
        (extract rankOf previous) + 1
      case (None, None) =>
        0
    }
  }
}
object ChannelOrderUpdater {
  def apply[A: HasChannelRank](db: SQLiteDatabase): ChannelOrderUpdater[A] = {
    new ChannelOrderUpdater(db)
  }
}

trait HasChannelRank[A] extends HasChannelStatusKey[A]{
  def rankOf(x: A): Double
}
