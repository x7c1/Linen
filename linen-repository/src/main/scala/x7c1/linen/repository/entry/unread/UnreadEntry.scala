package x7c1.linen.repository.entry.unread

import android.net.Uri
import x7c1.linen.database.struct.{HasAccountId, HasEntryId, HasSourceId}
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.action.SiteVisitable


trait UnreadEntry {
  def sourceId: Long
  def entryId: Long
  def accountId: Long
  def url: String
  def createdAt: Date

  def olderThan(entry: UnreadEntry): Boolean = {
    (createdAt.timestamp < entry.createdAt.timestamp) || {
      (createdAt.timestamp == entry.createdAt.timestamp) &&
      (entryId < entry.entryId)
    }
  }
}

object UnreadEntry {
  implicit def toSiteVisitable[A <: UnreadEntry]: SiteVisitable[A] =
    new SiteVisitable[A] {
      override def targetUri(target: A) = Uri parse target.url
    }

  implicit def id[A <: UnreadEntry]: HasEntryId[A] =
    new HasEntryId[A] {
      override def toId= _.entryId
    }

  implicit def sourceId[A <: UnreadEntry]: HasSourceId[A] =
    new HasSourceId[A] {
      override def toId = _.sourceId
    }

  implicit def accountId[A <: UnreadEntry]: HasAccountId[A] =
    new HasAccountId[A] {
      override def toId = _.accountId
    }
}

case class UnreadOutline(
  override val sourceId: Long,
  override val entryId: Long,
  override val url: String,
  override val createdAt: Date,
  override val accountId: Long,
  shortTitle: String,
  shortContent: String ) extends UnreadEntry

case class UnreadDetail(
  override val sourceId: Long,
  override val entryId: Long,
  override val url: String,
  override val createdAt: Date,
  override val accountId: Long,
  fullTitle: String,
  fullContent: String ) extends UnreadEntry
