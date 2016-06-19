package x7c1.wheat.modern.database

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.macros.database.Query.SqlBuilder


class QueryTest extends FlatSpecLike with Matchers {

  it can "build Query without args" in {
    val query = sql"SELECT * FROM foo WHERE id1 = 1"

    query.sql shouldBe "SELECT * FROM foo WHERE id1 = 1"
    query.selectionArgs shouldBe Seq()
  }
  it can "build Query with 1 arg" in {
    val id1 = 1
    val query = sql"SELECT * FROM foo WHERE id1 = $id1"

    query.sql shouldBe "SELECT * FROM foo WHERE id1 = ?"
    query.selectionArgs shouldBe Seq("1")
  }
  it can "build Query with 2 args" in {
    val id1 = 1
    val key2 = "key2"
    val query = sql"SELECT * FROM foo WHERE id1 = $id1 AND key2 = $key2"

    query.sql shouldBe "SELECT * FROM foo WHERE id1 = ? AND key2 = ?"
    query.selectionArgs shouldBe Seq("1", "key2")
  }
}
