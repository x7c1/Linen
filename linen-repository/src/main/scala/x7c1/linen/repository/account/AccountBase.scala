package x7c1.linen.repository.account

import android.database.Cursor
import x7c1.linen.database.mixin.TaggedAccountRecord
import x7c1.linen.database.mixin.TaggedAccountRecord.select
import x7c1.linen.database.struct.{AccountIdentifiable, ClientLabel, PresetLabel}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.ZeroAritySingle

object AccountBase {
  def apply(id: Long): AccountBase = new AccountBase {
    override def accountId: Long = id
  }
  implicit def id[A <: AccountBase]: AccountIdentifiable[A] =
    new AccountIdentifiable[A]{
      override def idOf(target: A) = target.accountId
    }
}

trait AccountBase {
  def accountId: Long
}

case class PresetAccount(accountId: Long) extends AccountBase

object PresetAccount {
  implicit object selectable extends ZeroAritySingle[PresetAccount](select(PresetLabel)){
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[TaggedAccountRecord](cursor) moveToHead reify
    }
  }
  private def reify(record: TaggedAccountRecord) = {
    PresetAccount(record.account_id)
  }
}

case class ClientAccount(accountId: Long) extends AccountBase

object ClientAccount {
  implicit object selectable extends ZeroAritySingle[ClientAccount](select(ClientLabel)){
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[TaggedAccountRecord](cursor) moveToHead reify
    }
  }
  private def reify(record: TaggedAccountRecord) = {
    ClientAccount(record.account_id)
  }
}
