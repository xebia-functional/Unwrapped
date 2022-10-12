package fx

import java.sql.SQLException
import scalikejdbc.*
import scalikejdbc.DB.*
import java.util.concurrent.atomic.AtomicInteger

class DatabaseSpec extends DatabaseSuite {

  val counter: AtomicInteger = new AtomicInteger(0)
  val names: Database[List[String]] = DB.readOnlyWithControl {
    case given DBSession =>
      counter.incrementAndGet()
      sql"select name from emp".map { rs => rs.string("name") }.list.apply()
  }
  testFX("Read only operation executes properly") {
    val result = structured(parallel(() => names, () => names))
    assertEqualsFX(result, (List("Ana", "Mike"), List("Ana", "Mike")))
    assertEqualsFX(counter.get(), 2)
  }

  testFX("Read only raises when tries to execute a non-read only operation") {
    assertFX(toEither(DB.readOnlyWithControl {
      case given DBSession =>
        sql"update emp set name = 'Never changed in a readonly operation' where id = 1"
          .update
          .apply()
    }).isLeft)
  }

  val txCounter: AtomicInteger = new AtomicInteger(0)
  val transactResult: Transaction[Int] = DB.localTransaction {
    case given DBSession =>
      txCounter.incrementAndGet()
      sql"update emp set name = 'Abc' where id = 1".update.apply()
      sql"update emp set name = 'Emp' where id = 2".update.apply()
  }
  testFX("Transaction goes well after updating 2 rows.") {
    val result = structured(parallel(() => transactResult, () => transactResult))
    assertEqualsFX(result, (1, 1))
    assertEqualsFX(txCounter.get(), 2)
  }

  val readAndWriteCounter: AtomicInteger = new AtomicInteger(0)
  val readAndWriteResult: Transaction[List[String]] = DB.localTransaction {
    case given DBSession =>
      readAndWriteCounter.incrementAndGet()
      sql"update emp set name = 'Claire' where id = 1".update.apply()
      sql"update emp set name = 'John' where id = 2".update.apply()
      sql"select name from emp".map { rs => rs.string("name") }.list.apply()
  }
  testFX("Transaction goes well after updating 2 rows and read the result.") {
    val result = structured(parallel(() => readAndWriteResult, () => readAndWriteResult))
    assertEqualsFX(result, (List("Claire", "John"), List("Claire", "John")))
    assertEqualsFX(txCounter.get(), 2)
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
