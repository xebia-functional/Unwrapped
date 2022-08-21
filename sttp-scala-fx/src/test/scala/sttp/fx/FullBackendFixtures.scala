package sttp
package fx

import _root_.fx.{given, _}
import com.sun.net.httpserver.*
import munit.fx.ScalaFXSuite
import sttp.client3.*
import sttp.model.headers.Accepts

import java.net.InetSocketAddress
import java.nio.file.Files
import java.time.*
import java.util.concurrent.Executors
import scala.io.Source
import scala.jdk.CollectionConverters.*
import java.nio.charset.StandardCharsets

private[fx] trait FullBackendFixtures { self: ScalaFXSuite =>

  def getRequest(baseServerAddress: String) = basicRequest
    .get(uri"$baseServerAddress/47DegLogo.svg")
    .header("Accepts", MediaTypes.image.`svg+xml`.value, true)

  val imageFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("47DegLogo", "svg").toFile(),
        (file, _) => file.deleteOnExit()
      ),
    teardown = _ => ())

  val resultFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("result", ".txt").toFile(),
        (file, _) => file.deleteOnExit()
      ),
    teardown = _ => ())

  val resultPutFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("resultPut", ".txt").toFile(),
        (file, _) => file.deleteOnExit()
      ),
    teardown = _ => ())

  val resultPatchFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("resultPatch", ".txt").toFile(),
        (file, _) => file.deleteOnExit()
      ),
    teardown = _ => ())

  val basicServer = httpServer(echoHandler)

  val testBody = FunFixture(setup = _ => "test", teardown = _ => ())

  val basicServerAndTestBody = FunFixture.map2(basicServer, testBody)

  val basicServerAndTestBodyAndFile = FunFixture.map3(basicServer, testBody, resultFileResource)

  val basicServerAndTestBodyAndPutFile =
    FunFixture.map3(basicServer, testBody, resultPutFileResource)

  val basicServerAndTestBodyAndPatchFile =
    FunFixture.map3(basicServer, testBody, resultPatchFileResource)

  val basicServerAndFile = FunFixture.map2(basicServer, imageFileResource)

  val executor = Executors.newVirtualThreadPerTaskExecutor
  sys.addShutdownHook(executor.shutdown())

  def httpServer(handler: HttpHandler) = FunFixture(
    setup = _ => {
      for {
        server <- Resource(
          HttpServer.create(InetSocketAddress(0), 0),
          (server, _) => server.stop(0)
        )
        _ = server.setExecutor(executor)
        _ = server.createContext("/", handler)
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
        case "POST" | "PATCH" | "PUT" => path.contains("echo")
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

        exchange.sendResponseHeaders(200, requestInputStream.available.toLong)
        requestInputStream.transferTo(outputStream)
      } catch {
        case _ => ()
      }
    },
    deleteHandler
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
        // mutableResponseHeaders.add("Content-Length", "988")
        exchange.sendResponseHeaders(200, 0)
        getClass.getResourceAsStream("/brand.svg").transferTo(outputStream)
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
    getImageHandler
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
