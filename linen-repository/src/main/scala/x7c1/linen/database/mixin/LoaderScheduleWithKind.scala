package x7c1.linen.database.mixin

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{HasAccountId, HasLoaderScheduleId, LoaderScheduleKindRecord, LoaderScheduleRecord}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.SelectorProvidable.CanReify
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, CanTraverseRecord, CanTraverseRecordByQuery, Find, FindBy, TraverseAll, TraverseOn}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}

trait LoaderScheduleWithKind
  extends LoaderScheduleRecord
    with LoaderScheduleKindRecord

object LoaderScheduleWithKind {
  implicit object id extends HasLoaderScheduleId[LoaderScheduleWithKind]{
    override def toId = _.schedule_id
  }
  implicit object providable
    extends SelectorProvidable[LoaderScheduleWithKind, Selector]

  implicit object reifiable extends RecordReifiable[LoaderScheduleWithKind]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleWithKind](cursor)
  }
  implicit object findable extends CanFindRecord[HasLoaderScheduleId, LoaderScheduleWithKind]{
    override def query[X: HasLoaderScheduleId](target: X): Query = {
      val scheduleId = implicitly[HasLoaderScheduleId[X]] toId target
      val sql = s"""
        |SELECT * FROM loader_schedules as t1
        | INNER JOIN loader_schedule_kinds as t2
        |   ON t1.schedule_kind_id = t2.schedule_kind_id
        |WHERE t1.schedule_id = ?
      """.stripMargin

      Query(sql, Array(scheduleId.toString))
    }
  }
  implicit object traverseOn extends CanTraverseRecord[HasAccountId, LoaderScheduleWithKind]{
    override def query[X: HasAccountId](target: X): Query = {
      val sql = s"""
        |SELECT * FROM loader_schedules as t1
        | INNER JOIN loader_schedule_kinds as t2
        |   ON t1.schedule_kind_id = t2.schedule_kind_id
        |WHERE t1.account_id = ?
      """.stripMargin

      val accountId = implicitly[HasAccountId[X]] toId target
      Query(sql, Array(accountId.toString))
    }
  }
  implicit object traverseAll
    extends CanTraverseRecordByQuery[LoaderScheduleWithKind](
      Query(
        s"""
          |SELECT * FROM loader_schedules as t1
          | INNER JOIN loader_schedule_kinds as t2
          |   ON t1.schedule_kind_id = t2.schedule_kind_id
          |ORDER BY t1.schedule_id ASC
         """.stripMargin
      )
    )

  class Selector(protected val db: SQLiteDatabase)
    extends Find[LoaderScheduleWithKind]
      with FindBy[HasLoaderScheduleId, LoaderScheduleWithKind]
      with TraverseOn[HasAccountId, LoaderScheduleWithKind]
      with TraverseAll[LoaderScheduleWithKind]

  object Selector {
    implicit def reify: CanReify[Selector] = new Selector(_)
  }
}
