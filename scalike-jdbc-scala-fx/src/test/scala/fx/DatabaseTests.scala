package fx

import munit.fx.ScalaFXSuite
import java.sql.SQLException
import scalikejdbc.*
import scalikejdbc.DB.*

class DatabaseTests extends ScalaFXSuite {

  Class.forName("org.postgresql.Driver")
  ConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalafx", "postgres", "postgres")

  given DB.CPContext = NoCPContext
  given SettingsProvider = SettingsProvider.default

  testFX("Read only operation executes properly") {
    val names: Database[List[String]] = DB.readOnlyWithControl { implicit session =>
      sql"select name from emp".map { rs => rs.string("name") }.list.apply()
    }
    assertFX(run(structured(names.join.toEither.isRight)) match {
      case b: Boolean => b
      case _ => false
    })
  }

  testFX("Read only shift when tries to execute a non-read only operation") {
    val names: Database[Int] = DB.readOnlyWithControl { implicit session =>
      sql"update emp set name = 'Abc' where id = 1".update.apply()
    }
    assertFX(run(structured(names.join.toEither.isLeft)) match {
      case b: Boolean => false
      case _ => true
    })
  }
}
