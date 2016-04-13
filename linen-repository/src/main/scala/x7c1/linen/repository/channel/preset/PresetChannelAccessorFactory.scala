package x7c1.linen.repository.channel.preset

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.PresetAccount
import x7c1.linen.repository.preset.{NoPresetAccount, PresetRecordError, UnexpectedException}
import x7c1.wheat.modern.database.Query

class PresetChannelAccessorFactory(queryFactory: PresetChannelQueryFactory){
  def create(
    clientAccountId: Long,
    helper: DatabaseHelper): Either[PresetRecordError, PresetChannelsAccessor] = {

    val presetAccount = helper.readable.find[PresetAccount]()
    val either = presetAccount match {
      case Left(error) => Left(UnexpectedException(error))
      case Right(None) => Left(NoPresetAccount())
      case Right(Some(preset)) => Right(preset.accountId)
    }
    either.right map { presetAccountId =>
      val query = queryFactory.createQuery(clientAccountId, presetAccountId)
      new PresetChannelAccessorImpl(helper, query, clientAccountId)
    }
  }
}

trait PresetChannelQueryFactory {
  def createQuery(clientAccountId: Long, presetAccountId: Long): Query
}

