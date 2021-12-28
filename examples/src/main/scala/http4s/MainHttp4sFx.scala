package examples.http4s

import fx._

import org.http4s.blaze.server.BlazeServerBuilder
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.IO
import cats.syntax.all._
import cats.data.Kleisli

import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}

import io.circe.generic.auto._

import scala.collection.mutable

//Assuming the cats IO binding is something like this
object IOBind:
  extension [A](io: IO[A])
    def bind: A % Bind = {
      import cats.effect.unsafe.IORuntime
      implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
      io.unsafeRunSync()
    }
end IOBind

case class FxUser(val id: Int, val email: String, val age: Int)

object FxMemoryRepo:
  private val db = mutable.Map[Int, FxUser](
    (1 -> FxUser(1, "a@mail.com", 27)),
    (2 -> FxUser(2, "b@mail.com", 34)),
    (3 -> FxUser(3, "c@mail.com", 41)),
    (4 -> FxUser(4, "d@mail.com", 48))
  )

  def findBy(id: Int): FxUser % Bind % Control[None.type] = db.get(id).bind
  def findAll: List[FxUser] % Bind = db.values.toList
  def delete(id: Int): Unit % Bind % Control[None.type] = db.remove(id).map(_ => ()).bind
  def save(FxUser: FxUser): Unit % Bind = db.addOne((FxUser.id -> FxUser))
  def update(id: Int, FxUser: FxUser): Unit % Bind = db.update(id, FxUser)
end FxMemoryRepo

object FxService:
  def findBy(id: Int): FxUser % Control[None.type] = FxMemoryRepo.findBy(id)
  def delete(id: Int): Unit % Control[None.type] = FxMemoryRepo.delete(id)
  def save(FxUser: FxUser): Unit % Bind = FxMemoryRepo.save(FxUser)
  def update(id: Int, FxUser: FxUser): Unit % Bind = FxMemoryRepo.update(id, FxUser)
  def findAll: List[FxUser] % Bind = FxMemoryRepo.findAll
end FxService

object FxRouting extends Http4sDsl[IO]:

  import IOBind._ // Lines 66, 70, 75, 79 Require IO Binding

  private val FxUserRoutes: HttpRoutes[IO] % Bind % Control[None.type] = HttpRoutes.of[IO] {
    case GET -> Root => Ok(IO(FxService.findAll).bind)

    case GET -> Root / IntVar(userId) =>
      Ok(IO(FxService.findBy(userId)).bind)

    case rq @ POST -> Root => {
      val FxUser = rq.asJsonDecode[FxUser].bind
      Created(IO(FxService.save(FxUser)).bind)
    }

    case rq @ PUT -> Root / IntVar(userId) => {
      val FxUser = rq.asJsonDecode[FxUser].bind
      Ok(IO(FxService.update(userId, FxUser)).bind)
    }

    case DELETE -> Root / IntVar(userId) =>
      Ok(IO(FxService.delete(userId)).bind)
  }

  val userService: Kleisli[IO, Request[IO], Response[IO]] % Control[None.type] = Router(
    "/users" -> FxUserRoutes).orNotFound

end FxRouting

@main def http4sFxExample =
  import IOBind._ // effect Require IO Binding
  import fx.runtime

  val effect: Unit % Bind % Control[None.type] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8081, "localhost")
      .withHttpApp(FxRouting.userService)
      .serve
      .compile
      .drain
      .bind

  run(effect)
