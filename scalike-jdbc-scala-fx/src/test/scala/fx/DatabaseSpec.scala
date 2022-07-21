package fx

import java.sql.SQLException
import scalikejdbc.*
import scalikejdbc.DB.*

class DatabaseSpec extends DatabaseSuite {

  testFX("Read only operation executes properly") {
    val names: Database[List[String]] = DB.readOnlyWithControl {
      case given DBSession =>
        sql"select name from emp".map { rs => rs.string("name") }.list.apply()
    }
    assertEqualsFX(names, List("Ana", "Mike"))
  }

  testFX("Read only shift when tries to execute a non-read only operation") {
    assertFX(toEither(DB.readOnlyWithControl {
      case given DBSession =>
        sql"update emp set name = 'Never changed in a readonly operation' where id = 1"
          .update
          .apply()
    }).isLeft)
  }

  testFX("Transaction goes well after updating 2 rows.") {
    val count: Transaction[Int] = DB.localTransaction {
      case given DBSession =>
        sql"update emp set name = 'Abc' where id = 1".update.apply()
        sql"update emp set name = 'Emp' where id = 2".update.apply()
    }
    assertEqualsFX(count, 1)
  }

  testFX("Transaction goes wrong and rollback after second update raises an exception") {
    assertFX(toEither(DB.localTransaction {
      case given DBSession =>
        sql"update emp set name = 'Never changed in a failed transaction' where id = 1"
          .update
          .apply()
        sql"update emp set name = 'Empty' where inventing_row = 0".update.apply()
    }).isLeft)
  }

}
