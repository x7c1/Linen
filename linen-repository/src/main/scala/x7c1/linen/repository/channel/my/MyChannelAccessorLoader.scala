package x7c1.linen.repository.channel.my

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.ClientAccount
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequence
import x7c1.wheat.modern.formatter.ThrowableFormatter.format


class MyChannelAccessorLoader(helper: DatabaseHelper){

  private var accessorHolder: Option[AccessorHolder] = None

  def reload(client: ClientAccount)(f: MyChannelAccessor => Unit): Unit = {
    accessorHolder -> createAccessor(client) match {
      case (Some(holder), Right(accessor)) =>
        holder updateAccessor accessor
        f(holder)
      case (None, Right(accessor)) =>
        val holder = new AccessorHolder(accessor)
        this.accessorHolder = Some(holder)
        f(holder)
      case (_, Left(exception)) =>
        Log error format(exception){"[unexpected]"}
    }
  }
  private def createAccessor(client: ClientAccount) = {
    val internal = ClosableMyChannelAccessor.create(helper, client)
    internal.right map (new SourceFooterAppender(_))
  }
  private class AccessorHolder(
    private var underlying: ClosableMyChannelAccessor) extends MyChannelAccessor {

    def updateAccessor(accessor: ClosableMyChannelAccessor) = synchronized {
      underlying.closeCursor()
      underlying = accessor
    }
    override def findAt(position: Int) = underlying findAt position

    override def length = underlying.length
  }
}

private object ClosableMyChannelAccessor {
  def create(
    helper: DatabaseHelper,
    client: ClientAccount): Either[Exception, ClosableMyChannelAccessor] = {

    try for {
      sequence <- helper.selectorOf[MyChannel].traverseOn(client).right
    } yield {
      new ClosableMyChannelAccessorImpl(sequence)
    } catch {
      case e: Exception => Left(e)
    }
  }
}

private trait ClosableMyChannelAccessor
  extends MyChannelAccessor
  with ClosableSequence[MyChannelRow]

private class ClosableMyChannelAccessorImpl (
  sequence: ClosableSequence[MyChannel]) extends ClosableMyChannelAccessor {

  override def findAt(position: Int) =
    sequence.findAt(position)

  override def length = sequence.length

  override def closeCursor(): Unit = sequence.closeCursor()
}

private class SourceFooterAppender(
  accessor: ClosableMyChannelAccessor) extends ClosableMyChannelAccessor{

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
