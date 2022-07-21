package fx

import java.sql.SQLException
import scalikejdbc.*
import scalikejdbc.DB.*

class DatabaseSpec extends DatabaseSuite {

  testFX("Read only operation executes properly") {
    val names: Database[List[String]] = DB.readOnlyWithControl { implicit session =>
      sql"select name from emp".map { rs => rs.string("name") }.list.apply()
    }
    assertEqualsFX(structured(names.join), List("Ana", "Mike"))
  }

  testFX("Read only shift when tries to execute a non-read only operation") {
    assertFX(toEither(structured(DB.readOnlyWithControl {
      case given DBSession =>
        sql"update emp set name = 'Never changed in a readonly operation' where id = 1"
          .update
          .apply()
    }).join).isLeft)
  }

  testFX("Transaction goes well after updating 2 rows.") {
    val count: Transaction[Int] = DB.localTransaction { implicit session =>
      sql"update emp set name = 'Abc' where id = 1".update.apply()
      sql"update emp set name = 'Emp' where id = 2".update.apply()
    }
    assertEqualsFX(run(structured(count.join)), 1)
  }

  testFX("Transaction goes wrong and rollback after second update raises an exception") {
    val count: Transaction[Int] = DB.localTransaction { implicit session =>
      sql"update emp set name = 'Never changed in a failed transaction' where id = 1"
        .update
        .apply()
      sql"update emp set name = 'Empty' where inventing_row = 0".update.apply()
    }
    assertFX(run(structured(count.join.toEither.isLeft)) match {
      case b: Boolean => false
      case _ => true
    })
  }

}
