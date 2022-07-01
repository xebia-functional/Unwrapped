package fx

import java.net.{http => jnh}
import munit.fx.ScalaFXSuite
import java.util.Optional
import java.net.CookieHandler
import java.time.Duration
import java.net.http.HttpClient.Redirect
import java.net.ProxySelector
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import java.net.Authenticator
import java.util.concurrent.Executor
import java.net.http.HttpResponse
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpHandlers
import java.net.InetSocketAddress
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.Request
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.net.URI

class HttpSuite extends ScalaFXSuite {

  serverExpectedResponseHeadersExecutorAndGetHandler.testFX(
    "a string get request to /ping should return pong") {
    case ((serverResource, expectedResponseHeaders, executor), getHandler) =>
      serverResource.use { server =>
        val executorSet: Unit = server.setExecutor(executor)
        val contextCreated: HttpContext =
          server.createContext("/ping", getHandler)
        val serverURI: InetSocketAddress = server.getAddress()
        val serverStart: Unit = server.start
        val pongResponse = structured(
          parallel(
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/1")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/2")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/3")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/4")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/5")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/6")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/7")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/8")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/9")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/0")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/11")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/12")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/13")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/14")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/15")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/16")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/17")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/18")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/19")).join.body,
            () => Http.GET[String](URI.create(s"http:/$serverURI/ping/20")).join.body,
          )
        )

        assertEqualsFX(
          pongResponse,
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
          ))
      }

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
  lazy val fallbackHttpHandler = HttpHandlers.of(404, notFoundHeaders, "Not Found")

  lazy val httpServer = FunFixture(
    setup = _ => {
      Resource(HttpServer.create(InetSocketAddress(0), 0), (server, _) => server.stop(0))
    },
    teardown = server => {
      ()
    })

  lazy val expectedGetHeaders = FunFixture(setup = _ => getSuccessHeaders, teardown = _ => ())

  lazy val getHttpHandler = FunFixture[HttpHandler](
    setup = _ =>
      HttpHandlers.handleOrElse(
        (request: Request) => {
          request
            .getRequestMethod() == "GET" && request.getRequestURI().getPath().contains("ping")
        },
        HttpHandlers.of(200, getSuccessHeaders, "pong"),
        fallbackHttpHandler
      ),
    teardown = _ => ()
  )

  lazy val virtualThreadExecutor = FunFixture(
    setup = _ => Executors.newVirtualThreadPerTaskExecutor,
    teardown = executor => {
      println("tearing down executor")
      executor.shutdown
    })

  lazy val serverExpectedResponseHeadersExecutorAndGetHandler = FunFixture.map2(
    FunFixture.map3(httpServer, expectedGetHeaders, virtualThreadExecutor),
    getHttpHandler)

}
