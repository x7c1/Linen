package x7c1.linen.modern.accessor.setting

import x7c1.linen.modern.accessor.preset.{NoPresetAccount, PresetAccount, PresetRecordError, UnexpectedException}
import x7c1.linen.modern.accessor.{LinenOpenHelper, Query}

object SelectedChannelsAccessor {
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
      val query = createQuery(clientAccountId, presetAccountId)
      val cursor = helper.getReadableDatabase.rawQuery(query.sql, query.selectionArgs)
      new PresetChannelAccessorImpl(cursor)
    }
  }
  def createQuery(clientAccountId: Long, presetAccountId: Long) = {
    val sql =
      s"""SELECT
         |  c1._id AS channel_id,
         |  c1.name AS name,
         |  c1.description AS description,
         |  IFNULL(c2.subscribed, 0) AS subscribed
         |FROM channels AS c1
         |  INNER JOIN channel_statuses AS c2
         |    ON c2.account_id = ? AND c1._id = c2.channel_id
         |WHERE c1.account_id = ?
         |ORDER BY c1._id DESC
       """.stripMargin

    new Query(sql, Array(
      clientAccountId.toString,
      presetAccountId.toString)
    )
  }
}
