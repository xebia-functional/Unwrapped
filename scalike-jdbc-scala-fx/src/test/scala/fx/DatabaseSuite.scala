package fx

import munit.fx.ScalaFXSuite

import scalikejdbc.*
import scalikejdbc.DB.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainerForAll
import org.flywaydb.core.Flyway
import org.testcontainers.utility.DockerImageName

class DatabaseSuite extends ScalaFXSuite with TestContainerForAll {

  private val postgresV = "12.6"

  private val dbName = "scalafx"
  private val dbUserName = "test"
  private val dbPassword = "password"
  private val driverName = "org.postgresql.Driver"

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    DockerImageName.parse(s"postgres:$postgresV"),
    databaseName = dbName,
    username = dbUserName,
    password = dbPassword
  )

  override def afterContainersStart(containers: PostgreSQLContainer): Unit =
    Class.forName(driverName)
    val ipAddress = containers.container.getHost
    val port = containers.container.getMappedPort(5432)
    ConnectionPool.singleton(containers.jdbcUrl, dbUserName, dbPassword)
    Flyway.configure.dataSource(containers.jdbcUrl, dbUserName, dbPassword).load.migrate
    given DB.CPContext = NoCPContext
    given SettingsProvider = SettingsProvider.default
}
