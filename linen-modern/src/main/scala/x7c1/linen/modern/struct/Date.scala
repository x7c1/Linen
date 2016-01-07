package x7c1.linen.modern.struct

import java.util

object Date {
  def dummy(): Date = new DateImpl()
  def timestamp: Int = (new util.Date().getTime / 1000).toInt
}

trait Date {
  def format: String
}

private class DateImpl extends Date {
  private val underlying = new util.Date()
  override def format: String = underlying.toString
}
