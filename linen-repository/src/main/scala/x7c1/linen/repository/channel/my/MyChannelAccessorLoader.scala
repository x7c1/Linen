package x7c1.linen.repository.channel.my

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequence
import x7c1.wheat.modern.formatter.ThrowableFormatter.format


class MyChannelAccessorLoader(helper: DatabaseHelper){

  private var accessorHolder: Option[AccessorHolder] = None

  def reload[A: HasAccountId](client: A)(f: MyChannelAccessor => Unit): Unit = {
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
  private def createAccessor[A: HasAccountId](client: A) = {
    val internal = helper.selectorOf[MyChannel] traverseOn client
    internal.right map (new SourceFooterAppender(_))
  }
  private class AccessorHolder(
    private var underlying: ClosableSequence[MyChannelRow]) extends MyChannelAccessor {

    def updateAccessor(accessor: ClosableSequence[MyChannelRow]) = synchronized {
      underlying.closeCursor()
      underlying = accessor
    }
    override def findAt(position: Int) = underlying findAt position

    override def length = underlying.length
  }
}

private class SourceFooterAppender(
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
