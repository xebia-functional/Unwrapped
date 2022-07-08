package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import java.sql.SQLException
import scalikejdbc.*
import scalikejdbc.DB.*

object DatabaseTests extends Properties("Database props"):

  Class.forName("org.postgresql.Driver")
  ConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalafx", "postgres", "postgres")

  property("Read only operation executes properly") = forAll { (s: String) =>

    given DB.CPContext = NoCPContext
    given SettingsProvider = SettingsProvider.default

    val names: Database[List[String]] = DB.readOnlyWithControl  { implicit session =>
      sql"select name from emp".map { rs => rs.string("name") }.list.apply()
    }
    run(structured(names.join.toEither.isRight)) match {
      case b: Boolean => b
      case _ => false
    }
  }

  property("Read only shift when tries to execute a non-read only operation") = forAll { (s: String) =>

    given DB.CPContext = NoCPContext
    given SettingsProvider = SettingsProvider.default

    val names: Database[Int] = DB.readOnlyWithControl  { implicit session =>
      sql"update emp set name = ${s} where id = 1".update.apply()
    }
    run(structured(names.join.toEither.isLeft)) match {
      case b: Boolean => false
      case _ => true
    }
  }
end DatabaseTests
