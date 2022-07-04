package fx

import com.sun.net.httpserver.*
import munit.fx.ScalaFXSuite

import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executors
import scala.jdk.CollectionConverters.*
import java.net.http.HttpHeaders
import java.util.concurrent.atomic.AtomicInteger

class HttpSuite extends ScalaFXSuite {

  httpServer(getHttpHandler(Option.empty))
    .testFX("requests should be returned in non-blocking fibers") { serverResource =>
      serverResource.use { baseServerAddress =>
        val pongResponse: Structured ?=> (
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String,
            String) =
          parallel(
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/1")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/2")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/3")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/4")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/5")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/6")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/7")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/8")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/9")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/0")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/11")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/12")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/13")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/14")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/15")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/16")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/17")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/18")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/19")).join.body,
            () => Http.GET[String](URI.create(s"$baseServerAddress/ping/20")).join.body
          )

        assertEqualsFX(
          structured(pongResponse),
          (
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong",
            "pong"
          )
        )
      }
    }

  httpServer(getHttpHandler(Option(Headers.of("X-Extended-Test-Header", "HttpSuite"))))
    .testFX("Expected headers are sent with requests, 404 means headers not sent") {
      (serverAddressResource: Resource[String]) =>
        serverAddressResource.use { baseServerAddress =>
          assertEqualsFX(
            structured(
              Http
                .GET[String](
                  URI.create(s"$baseServerAddress/ping/1"),
                  HttpHeader("X-Extended-Test-Header", "HttpSuite"))
                .join
                .body),
            "pong"
          )
        }
    }

  httpServer(getHttpFailureHandler(Option.empty)).testFX("By default, failed requests should retry 3 times"){ serverResource =>

    
  }

  lazy val notFoundHeaders = Headers.of(
    "Content-Type",
    "text/plain; charset=UTF-8",
    "Last-Modified",
    "Thu, 10 Mar 2022 23:21:48 GMT",
    "Connection",
    "keep-alive",
    "Date",
    "Fri, 01 Jul 2022 04:22:42 GMT"
  )
  lazy val getSuccessHeaders = Headers.of(
    "Content-Type",
    "text/plain; charset=UTF-8",
    "Last-Modified",
    "Thu, 10 Mar 2022 23:21:48 GMT",
    "Connection",
    "keep-alive",
    "Date",
    "Fri, 01 Jul 2022 04:22:42 GMT"
  )
  lazy val serverProblemHeaders = Headers.of(
    "Content-Type",
    "text/plain; charset=UTF-8",
    "Connection",
    "close",
    "Date",
    "Fri, 01 Jul 2022 04:22:42 GMT"
  )

  def httpServer(handler: HttpHandler) = FunFixture(
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

  def getHttpFailureHandler: HttpHandler = HttpHandlers.of(500, serverProblemHeaders, "Server Error")

  def getHttpHandler(maybeExpectedHeaders: Option[Headers]) = HttpHandlers.handleOrElse(
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

  lazy val virtualThreadExecutor = FunFixture(
    setup = _ => Executors.newVirtualThreadPerTaskExecutor,
    teardown = executor => {
      println("tearing down executor")
      executor.shutdown
    })
}
