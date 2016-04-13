package x7c1.linen.repository.account.setup

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{ClientLabel, PresetLabel, AccountParts}
import x7c1.linen.repository.account.{ClientAccount, PresetAccount}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.preset.PresetRecordError

object PresetAccountSetup {
  def apply(helper: DatabaseHelper): PresetAccountSetup = {
    new PresetAccountSetup(helper)
  }
}

class PresetAccountSetup (helper: DatabaseHelper) {
  private lazy val setup = new TaggedAccountSetup[PresetAccount](helper, PresetLabel)

  def findOrCreate(): Either[PresetRecordError, PresetAccount] = {
    val either = setup findOrCreate AccountParts(
      nickname = "Preset User",
      biography = "preset channels",
      createdAt = Date.current()
    )
    either.right map PresetAccount.apply
  }
}

object ClientAccountSetup {
  def apply(helper: DatabaseHelper): ClientAccountSetup = {
    new ClientAccountSetup(helper)
  }
}

class ClientAccountSetup private (helper: DatabaseHelper){
  private lazy val setup = new TaggedAccountSetup[ClientAccount](helper, ClientLabel)

  def findOrCreate(): Either[PresetRecordError, ClientAccount] = {
    val either = setup findOrCreate AccountParts(
      nickname = "no name",
      biography = "no profile",
      createdAt = Date.current()
    )
    either.right map ClientAccount.apply
  }
}
