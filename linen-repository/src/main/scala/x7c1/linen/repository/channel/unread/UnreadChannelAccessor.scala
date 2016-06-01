package x7c1.linen.repository.channel.unread

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasChannelId
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.channel.unread.ChannelLoaderEvent.{AccessorError, Done}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.TaskProvider.async
import x7c1.wheat.modern.database.selector.presets.ClosableSequence
import x7c1.wheat.modern.sequence.Sequence


trait ChannelSelectable[A] extends HasChannelId[A]{
  def nameOf: A => String
}

class UnreadChannelLoader(helper: DatabaseHelper, client: ClientAccount){
  private lazy val holder = new AccessorHolder

  lazy val accessor: Sequence[UnreadChannel] = holder

  accessor.map(_.name).length

  def startLoading(): CallbackTask[ChannelLoaderEvent] = async {
    Log info s"[start]"
    helper.selectorOf[UnreadChannel].traverseOn(client)
  } map {
    case Right(loadedAccessor) =>
      Log info s"[done]"

      loadedAccessor.map(_.name).closeCursor()

      holder updateAccessor loadedAccessor
      new Done(client, loadedAccessor.findAt(0))
    case Left(error) =>
      Log info s"[failed]"
      new AccessorError(error)
  }
  private class AccessorHolder extends Sequence[UnreadChannel] {
    private var underlying: Option[ClosableSequence[UnreadChannel]] = None

    def updateAccessor(accessor: ClosableSequence[UnreadChannel]): Unit = synchronized {
      underlying foreach {_.closeCursor()}
      underlying = Some(accessor)
    }
    override def findAt(position: Int): Option[UnreadChannel] = {
      underlying flatMap (_ findAt position)
    }
    override def length: Int = {
      underlying map (_.length) getOrElse 0
    }
  }
}
