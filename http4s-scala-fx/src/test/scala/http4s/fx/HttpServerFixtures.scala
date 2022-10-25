package http4s
package fx

import _root_.fx.{handle, structured, toEither, Control, Resource}
import _root_.fx.instances.FxAsync.asyncInstance
import _root_.fx.instances.StructuredF
import _root_.fx.HttpExecutionException
import fx.BlazeServerFXBackend
import munit.FunSuite
import org.http4s.Method.{GET, POST}
import org.http4s.{HttpApp, HttpRoutes, Response}

trait HttpServerFixtures:
  self: FunSuite =>

  val pingPongApp: HttpApp[StructuredF] =
    HttpApp.apply {
      case r if r.method == GET && r.uri.path.renderString.contains("ping") =>
        Response[StructuredF]().withEntity[String]("pong")
    }

  val echoApp: HttpApp[StructuredF] =
    HttpApp.apply {
      case r if r.method == POST && r.uri.path.renderString.contains("echo") =>
        Response[StructuredF]().withBodyStream(r.body)
    }

  def httpServerHttp4s(app: HttpApp[StructuredF]): FunFixture[Resource[String]] =
    FunFixture(
      setup = _ => {
        for {
          server <- toEither(
            structured(BlazeServerFXBackend(app))
          ).fold(e => throw e, identity)
        } yield s"http:/${server.server().address}"
      },
      teardown = _ => ()
    )
