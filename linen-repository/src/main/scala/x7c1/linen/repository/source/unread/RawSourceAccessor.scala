package x7c1.linen.repository.source.unread

import x7c1.linen.database.control.DatabaseHelper
import x7c1.wheat.macros.logger.Log

class RawSourceAccessor(helper: DatabaseHelper){

  private lazy val readable = helper.readable

  def findTitleOf(sourceId: Long): Option[String] = {
    helper.selectorOf[SourceTitle] findBy sourceId via {
      case Left(exception) =>
        Log error exception.getMessage
        None
      case Right(row) => row map (_.title)
    }
  }
}
