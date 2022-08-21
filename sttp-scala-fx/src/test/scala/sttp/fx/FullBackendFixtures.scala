package sttp
package fx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite
import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import scala.jdk.CollectionConverters.*
import java.time.*
import scala.io.Source

private[fx] trait FullBackendFixtures { self: ScalaFXSuite =>

  def httpServer(handlers: HttpHandler*) = FunFixture(
    setup = _ => {
      for {
        server <- Resource(
          HttpServer.create(InetSocketAddress(0), 0),
          (server, _) => server.stop(0)
        )
        serverExecutor <- Resource(
          Executors.newVirtualThreadPerTaskExecutor,
          (executor, _) => executor.shutdown()
        )
        _ = server.setExecutor(serverExecutor)
        _ = for {
          handler <- handlers
        } server.createContext("/", handler)
        _ = server.start
      } yield s"http:/${server.getAddress()}/"
    },
    teardown = _ => ()
  )

  /**
   * Echos what gets sent to the server back to the requester. This allows us to test pretty
   * much anything.
   */
  lazy val echoHandler = HttpHandlers.handleOrElse(
    request => {
      val method = request.getRequestMethod()
      val path = request.getRequestURI().getPath()
      method match {
        case "POST" | "PATCH" | "PUT" => path.contains("/echo")
        case _ => false
      }
    },
    (exchange: HttpExchange) => {
      try {
        val requestInputStream = exchange.getRequestBody()
        val outputStream = exchange.getResponseBody()
        val requestHeaders = exchange.getRequestHeaders()
        val requestHeaderKeys = requestHeaders.keySet().iterator().asScala
        val mutableResponseHeaders = exchange.getResponseHeaders()
        for {
          key <- requestHeaderKeys
        } mutableResponseHeaders.put(key, requestHeaders.get(key))
        exchange.sendResponseHeaders(200, 0L)
        requestInputStream.transferTo(outputStream)
        outputStream.flush()
        outputStream.close()
      } catch {
        case _ => ()
      } finally {
        exchange.close()
      }
    },
    fallbackHttpHandler
  )

  lazy val getImageHandler = HttpHandlers.handleOrElse(
    request => {
      val method = request.getRequestMethod()
      val path = request.getRequestURI().getPath()
      method match {
        case "GET" => path.contains("47DegLogo.svg")
        case _ => false
      }
    },
    (exchange: HttpExchange) => {
      try {
        val outputStream = exchange.getResponseBody()
        val mutableResponseHeaders = exchange.getResponseHeaders()
        mutableResponseHeaders.add("Content-Type", MediaTypes.image.`svg+xml`.value)
        mutableResponseHeaders.add("Content-Length", "988")
        exchange.sendResponseHeaders(200, 988L)
        getClass.getResourceAsStream("brand.svg").transferTo(outputStream)
        outputStream.flush()
        outputStream.close()
      } catch {
        case _ => ()
      } finally {
        exchange.close()
      }
    },
    fallbackHttpHandler
  )

  lazy val deleteHandler = HttpHandlers.handleOrElse(
    request => {
      val method = request.getRequestMethod()
      val path = request.getRequestURI().getPath()
      method match {
        case "DELETE" => path.contains("toDelete")
        case _ => false
      }
    },
    HttpHandlers.of(OK.value, getSuccessHeaders, OK.statusText),
    fallbackHttpHandler
  )

  lazy val fallbackHttpHandler =
    HttpHandlers.of(404, notFoundHeaders, "Not Found")

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

}
