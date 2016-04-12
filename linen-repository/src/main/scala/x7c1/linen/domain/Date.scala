package x7c1.linen.domain

import java.text.SimpleDateFormat
import java.util
import java.util.{Locale, TimeZone}

import x7c1.wheat.macros.database.FieldConvertible

object Date {
  def current(): Date = new DateImpl(new util.Date)

  def timestamp: Int = current().timestamp

  def apply(timestamp: Int): Date = {
    new DateImpl(new util.Date(timestamp.toLong * 1000))
  }
  def apply(date: util.Date): Date = new DateImpl(date)

  implicit object convertible extends FieldConvertible[Int, Date] {
    override def wrap(value: Int): Date = Date(value)
    override def unwrap(value: Date): Int = value.timestamp
  }
  private lazy val dateFormat: SimpleDateFormat = {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    format setTimeZone TimeZone.getDefault
    format
  }
  private class DateImpl(underlying: util.Date) extends Date {
    override lazy val format = dateFormat format underlying
    override lazy val timestamp = (underlying.getTime / 1000).toInt
  }
}

trait Date {
  def format: String
  def timestamp: Int
}
