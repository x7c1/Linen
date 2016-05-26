package x7c1.linen.database.mixin

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{AccountIdentifiable, LoaderScheduleKindRecord, LoaderScheduleLike, LoaderScheduleRecord}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, CanTraverseRecord, CanTraverseRecordByQuery, Find, FindBy, TraverseAll, TraverseOn}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}

trait LoaderScheduleWithKind
  extends LoaderScheduleRecord
    with LoaderScheduleKindRecord

object LoaderScheduleWithKind {
  implicit object id extends LoaderScheduleLike[LoaderScheduleWithKind]{
    override def toId = _.schedule_id
  }
  implicit object providable
    extends SelectorProvidable[LoaderScheduleWithKind, Selector](new Selector(_))

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
  implicit object traverseOn extends CanTraverseRecord[AccountIdentifiable, LoaderScheduleWithKind]{
    override def query[X: AccountIdentifiable](target: X): Query = {
      val sql = s"""
        |SELECT * FROM loader_schedules as t1
        | INNER JOIN loader_schedule_kinds as t2
        |   ON t1.schedule_kind_id = t2.schedule_kind_id
        |WHERE t1.account_id = ?
      """.stripMargin

      val accountId = implicitly[AccountIdentifiable[X]] toId target
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
      with FindBy[LoaderScheduleLike, LoaderScheduleWithKind]
      with TraverseOn[AccountIdentifiable, LoaderScheduleWithKind]
      with TraverseAll[LoaderScheduleWithKind]
}
