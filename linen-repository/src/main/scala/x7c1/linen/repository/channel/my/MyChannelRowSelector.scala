package x7c1.linen.repository.channel.my

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasAccountId
import x7c1.wheat.modern.database.selector.SelectorProvidable.CanReify
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequence, TraverseOn}

class MyChannelRowSelector (protected val db: SQLiteDatabase)
  extends TraverseOn[HasAccountId, MyChannelRow]

object MyChannelRowSelector {
  implicit def reify: CanReify[MyChannelRowSelector] = new MyChannelRowSelector(_)
}

private[channel] class CanTraverseImpl extends CanTraverse[HasAccountId, MyChannelRow]{
  override def extract[X: HasAccountId](db: SQLiteDatabase, account: X) = {
    val internal = db.selectorOf[MyChannel] traverseOn account
    internal.right map (new SourceFooterAppender(_))
  }
}

private[channel] class SourceFooterAppender(
  accessor: ClosableSequence[MyChannelRow]) extends ClosableSequence[MyChannelRow]{

  override def findAt(position: Int) = {
    if (position == accessor.length){
      Some(MyChannelFooter())
    } else {
      accessor findAt position
    }
  }
  override def length: Int = {
    // +1 to append Footer
    accessor.length + 1
  }
  override def closeCursor() = accessor.closeCursor()
}
