package x7c1.linen.repository.source.unread

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.SourceIdentifiable
import x7c1.wheat.macros.logger.Log

class RawSourceAccessor(helper: DatabaseHelper){

  private lazy val selector = helper.selectorOf[SourceTitle]

  def findTitleOf[A: SourceIdentifiable](sourceId: A): Option[String] = {
    selector findBy sourceId via {
      case Left(exception) =>
        Log error exception.getMessage
        None
      case Right(row) => row map (_.title)
    }
  }
}
