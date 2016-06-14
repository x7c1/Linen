package x7c1.linen.repository.account

import x7c1.linen.database.mixin.TaggedAccountRecord
import x7c1.linen.database.mixin.TaggedAccountRecord.select
import x7c1.linen.database.struct.{ClientLabel, HasAccountId, HasNamedChannelKey, NamedChannelKey, PresetLabel}
import x7c1.linen.repository.channel.preset.PresetChannelPiece
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{CanFindEntityByQuery, DefaultProvidable}

object AccountBase {
  def apply(id: Long): AccountBase = new AccountBase {
    override def accountId: Long = id
  }
  implicit def id[A <: AccountBase]: HasAccountId[A] =
    new HasAccountId[A]{
      override def toId = _.accountId
    }

  implicit def providable[A <: AccountBase]: DefaultProvidable[HasAccountId, A] =
    new DefaultProvidable[HasAccountId, A]
}

trait AccountBase {
  def accountId: Long
}

case class PresetAccount(accountId: Long) extends AccountBase

object PresetAccount {
  implicit object convertible extends CursorConvertible[TaggedAccountRecord, PresetAccount]{
    override def convertFrom = cursor =>
      PresetAccount(cursor.account_id)
  }
  implicit object query extends CanFindEntityByQuery
    [TaggedAccountRecord, PresetAccount](select(PresetLabel))

  implicit object channelKey extends HasNamedChannelKey[(PresetAccount, PresetChannelPiece)]{
    override def toId = {
      case (account, piece) =>
        NamedChannelKey(
          accountId = account.accountId,
          channelName = piece.name
        )
    }
  }
}

case class ClientAccount(accountId: Long) extends AccountBase

object ClientAccount {
  implicit object convertible extends CursorConvertible[TaggedAccountRecord, ClientAccount]{
    override def convertFrom = cursor =>
      ClientAccount(cursor.account_id)
  }
  implicit object query extends CanFindEntityByQuery
    [TaggedAccountRecord, ClientAccount](select(ClientLabel))
}
