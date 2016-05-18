package x7c1.linen.repository.channel.my

import x7c1.linen.database.control.DatabaseHelper
import x7c1.wheat.modern.sequence.Sequence

trait MyChannelAccessor extends Sequence[MyChannelRow]{
}

object MyChannelAccessor {
  def createForDebug(helper: DatabaseHelper, accountId: Long): MyChannelAccessor = {
    helper.selectorOf[MyChannel] traverseOn accountId match {
      case Left(e) => throw e
      case Right(accessor) => apply(accessor)
    }
  }
  private def apply(sequence: Sequence[MyChannelRow]): MyChannelAccessor =
    new MyChannelAccessor {
      override def findAt(position: Int) = sequence findAt position
      override def length = sequence.length
    }

}
