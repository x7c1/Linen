package x7c1.linen.modern.struct

import java.util

object Date {
  def current(): Date = new DateImpl()
  def timestamp: Int = current().timestamp
}

trait Date {
  def format: String
  def timestamp: Int
}

private class DateImpl extends Date {
  private val underlying = new util.Date()
  override def format: String = underlying.toString
  override def timestamp: Int = (underlying.getTime / 1000).toInt
}
