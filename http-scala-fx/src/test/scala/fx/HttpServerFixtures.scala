package fx

import munit.FunSuite

import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

trait HttpServerFixtures:
  self: FunSuite =>

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

  lazy val serverProblemHeaders =
    Headers.of(
      "Content-Type",
      "text/plain; charset=UTF-8",
      "Connection",
      "close",
      "Date",
      "Fri, 01 Jul 2022 04:22:42 GMT"
    )

  lazy val patchHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "PATCH" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val traceHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "TRACE" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val optionsHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request.getRequestMethod() == "OPTIONS" && request
          .getRequestURI()
          .getPath()
          .contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val deleteHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "DELETE" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(204, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val putHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "PUT" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(204, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val headHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "HEAD" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, "OK"),
      fallbackHttpHandler
    )

  lazy val postHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      (request: Request) => {
        request
          .getRequestMethod() == "POST" && request.getRequestURI().getPath().contains("ping")
      },
      HttpHandlers.of(201, getSuccessHeaders, "Created"),
      fallbackHttpHandler
    )

  def httpServer(handler: HttpHandler) =
    FunFixture(
      setup = _ => {
        for {
          server <- Resource(
            HttpServer.create(InetSocketAddress(0), 0),
            (server, _) => server.stop(0))
          serverExecutor <- Resource(
            Executors.newVirtualThreadPerTaskExecutor,
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

  def getHttpFailureHandler(
      maybeExpectedHeaders: Option[Headers],
      numRequiredFailures: AtomicInteger): HttpHandler =
    val serverFail = HttpHandlers.of(500, serverProblemHeaders, "Server Error")
    val successHandler = getHttpHandler(maybeExpectedHeaders)
    HttpHandlers.handleOrElse(
      _ => {
        numRequiredFailures.getAndDecrement > 0
      },
      serverFail,
      successHandler
    )

  lazy val fallbackHttpHandler =
    HttpHandlers.of(404, notFoundHeaders, "Not Found")

  def getHttpHandler(maybeExpectedHeaders: Option[Headers]) =
    HttpHandlers.handleOrElse(
      (request: Request) => {
        val hasAllExpectedHeaders = maybeExpectedHeaders.map {
          _.entrySet.asScala.forall { entrySet =>
            val headerName = entrySet.getKey
            request.getRequestHeaders.containsKey(headerName) && request
              .getRequestHeaders
              .get(headerName)
              .containsAll(entrySet.getValue)
          }
        }
        hasAllExpectedHeaders.getOrElse(true) && request
          .getRequestMethod() == "GET" && request.getRequestURI().getPath().contains("ping")
      },
      HttpHandlers.of(200, getSuccessHeaders, "pong"),
      fallbackHttpHandler
    )

  lazy val virtualThreadExecutor =
    FunFixture(
      setup = _ => Executors.newVirtualThreadPerTaskExecutor,
      teardown = executor => {
        println("tearing down executor")
        executor.shutdown
      })
