package examples.http4s

import org.http4s.blaze.server.BlazeServerBuilder
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.IO
import cats.syntax.all._

import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}

import io.circe.generic.auto._

import scala.collection.mutable
import cats.effect.ExitCode
import cats.effect.IOApp

case class User(val id: Int, val email: String, val age: Int)

object MemoryRepo:
  private val db = mutable.Map[Int, User](
    (1 -> User(1, "a@mail.com", 27)),
    (2 -> User(2, "b@mail.com", 34)),
    (3 -> User(3, "c@mail.com", 41)),
    (4 -> User(4, "d@mail.com", 48))
  )

  def findBy(id: Int): Option[User] = db.get(id)
  def findAll: List[User] = db.values.toList
  def save(user: User): Unit = db.addOne((user.id -> user))
  def update(id: Int, user: User): Unit = db.update(id, user)
  def delete(id: Int): Option[Unit] = db.remove(id).map(_ => ())
end MemoryRepo

object Service:
  def findBy(id: Int): Option[User] = MemoryRepo.findBy(id)
  def save(user: User): Unit = MemoryRepo.save(user)
  def update(id: Int, user: User): Unit = MemoryRepo.update(id, user)
  def findAll: List[User] = MemoryRepo.findAll
  def delete(id: Int): Option[Unit] = MemoryRepo.delete(id)
end Service

object Routing extends Http4sDsl[IO]:

  private val userRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => Ok(IO(Service.findAll))

    case GET -> Root / IntVar(userId) =>
      Ok(IO(Service.findBy(userId)))

    case rq @ POST -> Root =>
      rq.asJsonDecode[User].flatMap(user => IO(Service.save(user))).flatMap(Created(_))

    case rq @ PUT -> Root / IntVar(userId) =>
      rq.asJsonDecode[User].flatMap(user => IO(Service.update(userId, user))).flatMap(Ok(_))

    case DELETE -> Root / IntVar(userId) =>
      IO(Service.delete(userId)).flatMap(Ok(_))
  }

  val userService = Router("/users" -> userRoutes).orNotFound

end Routing

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(Routing.userService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
