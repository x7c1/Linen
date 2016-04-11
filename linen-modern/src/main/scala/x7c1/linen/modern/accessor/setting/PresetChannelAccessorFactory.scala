package x7c1.linen.modern.accessor.setting

import x7c1.linen.modern.accessor.{LinenOpenHelper, Query}
import x7c1.linen.modern.accessor.preset.{PresetRecordError, PresetAccount, UnexpectedException, NoPresetAccount}

class PresetChannelAccessorFactory(queryFactory: PresetChannelQueryFactory){
  def create(
    clientAccountId: Long,
    helper: LinenOpenHelper): Either[PresetRecordError, PresetChannelsAccessor] = {

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

