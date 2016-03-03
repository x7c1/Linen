package x7c1.linen.modern.accessor

import x7c1.linen.modern.init.dev.SourceTitle
import x7c1.wheat.macros.logger.Log

class RawSourceAccessor(helper: LinenOpenHelper){

  private lazy val readable = helper.readable

  def findTitleOf(sourceId: Long): Option[String] = {
    readable.find[SourceTitle].by(sourceId) match {
      case Left(exception) =>
        Log error exception.getMessage
        None
      case Right(row) => row map (_.title)
    }
  }
}
