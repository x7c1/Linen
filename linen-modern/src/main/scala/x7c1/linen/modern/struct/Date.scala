package x7c1.linen.modern.struct

import java.util

object Date {
  def current(): Date = new DateImpl(new util.Date)

  def timestamp: Int = current().timestamp

  def apply(timestamp: Int): Date = {
    new DateImpl(new util.Date(timestamp * 1000))
  }
  private class DateImpl(underlying: util.Date) extends Date {
    override def format: String = underlying.toString
    override def timestamp: Int = (underlying.getTime / 1000).toInt
  }
}

trait Date {
  def format: String
  def timestamp: Int
}
