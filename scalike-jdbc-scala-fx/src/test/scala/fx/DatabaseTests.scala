package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import java.sql.SQLException
import scalikejdbc._

object DatabaseTests extends Properties("Database props"):

  Class.forName("org.postgresql.Driver")
  ConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalafx", "postgres", "postgres")

  property("Read only operation executes properly") = forAll { (s: String) =>
    val names = DB.readOnlyWithControl  { implicit session =>
      sql"select name from emp".map { rs => rs.string("name") }.list.apply()
    }
    structured(names.join.toEither.isRight)
  }
end DatabaseTests
