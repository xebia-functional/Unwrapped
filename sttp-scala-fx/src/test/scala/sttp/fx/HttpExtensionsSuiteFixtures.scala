package sttp
package fx

import _root_.fx.{Accepted, Created}
import _root_.fx.Resource
import com.sun.net.httpserver.*
import munit.fx.ScalaFXSuite
import sttp.client3.StringBody

import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executors

trait HttpExtensionsSuiteFixtures { self: ScalaFXSuite =>
  val uri = FunFixture(setup = _ => URI("https://47Deg.com"), teardown = _ => ())

  val stringBody = FunFixture(setup = _ => StringBody("hello", "UTF-8"), teardown = _ => ())

  def httpServer(handler: HttpHandler) =
    FunFixture(
      setup = _ => {
        for {
          server <- Resource(
            () => HttpServer.create(InetSocketAddress(0), 0),
            (server, _) => server.stop(0))
          serverExecutor <- Resource(
            () => Executors.newVirtualThreadPerTaskExecutor,
            (executor, _) => executor.shutdown())
          _ = server.setExecutor(serverExecutor)
          httpContext = server.createContext("/root", handler)
          _ = server.start
        } yield s"http:/${server.getAddress()}/root"
      },
      teardown = server => {
        ()
      }
    )

  lazy val notFoundHeaders =
    Headers.of(
      "Content-Type",
      "text/plain; charset=UTF-8",
      "Last-Modified",
      "Thu, 10 Mar 2022 23:21:48 GMT",
      "Connection",
      "keep-alive",
      "Date",
      "Fri, 01 Jul 2022 04:22:42 GMT"
    )

  lazy val getSuccessHeaders =
    Headers.of(
      "Content-Type",
      "text/plain; charset=UTF-8",
      "Last-Modified",
      "Thu, 10 Mar 2022 23:21:48 GMT",
      "Connection",
      "keep-alive",
      "Date",
      "Fri, 01 Jul 2022 04:22:42 GMT"
    )

  lazy val fallbackHttpHandler =
    HttpHandlers.of(404, notFoundHeaders, "Not Found")

  lazy val patchHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "PATCH" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(Accepted.value, getSuccessHeaders, Accepted.statusText),
      fallbackHttpHandler
    )

  lazy val postHandler = HttpHandlers.handleOrElse(
    request =>
      request
        .getRequestMethod() == "POST" && request.getRequestURI().getPath().contains("ping"),
    HttpHandlers.of(Created.value, getSuccessHeaders, Created.statusText),
    fallbackHttpHandler
  )

  lazy val putHandler = HttpHandlers.handleOrElse(
    request =>
      request.getRequestMethod() == "PUT" && request.getRequestURI().getPath().contains("ping"),
    HttpHandlers.of(Accepted.value, getSuccessHeaders, Accepted.statusText),
    fallbackHttpHandler
  )

}
