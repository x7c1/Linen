package x7c1.linen.repository.channel.preset

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.ChannelParts
import x7c1.linen.repository.account.PresetAccount
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.preset.{PresetRecordError, UnexpectedException}
import x7c1.wheat.modern.database.WritableDatabase

class PresetChannelSetup private (helper: DatabaseHelper, account: PresetAccount) {
  def findOrCreate(piece: PresetChannelPiece): Either[PresetRecordError, PresetChannel] = {
    helper.readable.find[PresetChannel] by (account -> piece) via {
      case Right(Some(x)) => Right(x)
      case Right(None) => createPresetChannel(account, piece)
      case Left(error) => Left(UnexpectedException(error))
    }
  }
  private def createPresetChannel(
    account: PresetAccount,
    piece: PresetChannelPiece): Either[PresetRecordError, PresetChannel] =

    WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      val factory = new PresetChannelFactory(writable, account)
      for {
        channelId <- factory.insertChannel(piece).right
      } yield PresetChannel (
        channelId = channelId,
        accountId = account.accountId,
        name = piece.name
      )
    }
}

object PresetChannelSetup {
  def apply(helper: DatabaseHelper, account: PresetAccount): PresetChannelSetup = {
    new PresetChannelSetup(helper, account)
  }
}

class PresetChannelFactory(writable: WritableDatabase, account: PresetAccount){
  def insertChannel(piece: PresetChannelPiece): Either[PresetRecordError, Long] = {
    val either = writable insert ChannelParts(
      accountId = account.accountId,
      name = piece.name,
      description = piece.description,
      createdAt = Date.current()
    )
    either.left map UnexpectedException
  }
}
