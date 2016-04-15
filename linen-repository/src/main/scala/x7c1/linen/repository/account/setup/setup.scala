package x7c1.linen.repository.account.setup

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountParts, ClientLabel, PresetLabel}
import x7c1.linen.repository.account.{ClientAccount, PresetAccount}
import x7c1.linen.repository.date.Date


object PresetAccountSetup {
  def apply(helper: DatabaseHelper): PresetAccountSetup = {
    new PresetAccountSetup(helper)
  }
}

class PresetAccountSetup (helper: DatabaseHelper) {
  private lazy val setup = new TaggedAccountSetup[PresetAccount](helper, PresetLabel)

  def findOrCreate(): Either[AccountSetupError, PresetAccount] = {
    val either = setup findOrCreate AccountParts(
      nickname = "preset user",
      biography = "owner of preset channels",
      createdAt = Date.current()
    )
    either.right map (PresetAccount(_))
  }
}

object ClientAccountSetup {
  def apply(helper: DatabaseHelper): ClientAccountSetup = {
    new ClientAccountSetup(helper)
  }
}

class ClientAccountSetup private (helper: DatabaseHelper){
  private lazy val setup = new TaggedAccountSetup[ClientAccount](helper, ClientLabel)

  def findOrCreate(): Either[AccountSetupError, ClientAccount] = {
    val either = setup findOrCreate AccountParts(
      nickname = "default name",
      biography = "",
      createdAt = Date.current()
    )
    either.right map (ClientAccount(_))
  }
}
