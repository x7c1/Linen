package x7c1.linen.modern.accessor.setting

import x7c1.linen.modern.accessor.Query

object AllPresetChannelsAccessor
  extends PresetChannelAccessorFactory(AllPresetChannelsQuery)

object AllPresetChannelsQuery extends PresetChannelQueryFactory {
  override def createQuery(clientAccountId: Long, presetAccountId: Long) = {
    val sql =
      s"""SELECT
         |  c1._id AS channel_id,
         |  c1.name AS name,
         |  c1.description AS description,
         |  IFNULL(c2.subscribed, 0) AS subscribed
         |FROM channels AS c1
         |  LEFT JOIN channel_statuses AS c2
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
