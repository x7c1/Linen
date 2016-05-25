package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.{LoaderScheduleKindRecord, LoaderScheduleLike, LoaderScheduleRecord}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, CanTraverseRecordByQuery, DefaultProvidable}

trait LoaderScheduleWithKind
  extends LoaderScheduleRecord
    with LoaderScheduleKindRecord

object LoaderScheduleWithKind {
  implicit object id extends LoaderScheduleLike[LoaderScheduleWithKind]{
    override def toId = _.schedule_id
  }
  implicit object providable
    extends DefaultProvidable[LoaderScheduleLike, LoaderScheduleWithKind]

  implicit object reifiable extends RecordReifiable[LoaderScheduleWithKind]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleWithKind](cursor)
  }
  implicit object findable extends CanFindRecord[LoaderScheduleLike, LoaderScheduleWithKind]{
    override def query[X: LoaderScheduleLike](target: X): Query = {
      val scheduleId = implicitly[LoaderScheduleLike[X]] toId target
      val sql = s"""
        |SELECT * FROM loader_schedules as t1
        | INNER JOIN loader_schedule_kinds as t2
        |   ON t1.schedule_kind_id = t2.schedule_kind_id
        |WHERE t1.schedule_id = ?
      """.stripMargin

      Query(sql, Array(scheduleId.toString))
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
}
