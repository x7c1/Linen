package x7c1.linen.repository.account

import android.database.Cursor
import x7c1.linen.database.mixin.TaggedAccountRecord
import x7c1.linen.database.mixin.TaggedAccountRecord.select
import x7c1.linen.database.struct.{ClientLabel, PresetLabel}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.ZeroAritySingle

object AccountIdentifiable {
  def apply(id: Long): AccountIdentifiable = new AccountIdentifiable {
    override def accountId: Long = id
  }
}

trait AccountIdentifiable {
  def accountId: Long
}

case class PresetAccount(accountId: Long) extends AccountIdentifiable

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

case class ClientAccount(accountId: Long) extends AccountIdentifiable

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