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
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ExecutorService

private[fx] trait FullBackendFixtures { self: ScalaFXSuite =>

  val server = new AtomicReference[HttpServer]()
  val executor = new AtomicReference[ExecutorService](Executors.newVirtualThreadPerTaskExecutor)

  def getServerAddress() = {
    s"http:/${server.get.getAddress()}/"
  }

  override def beforeAll() = {
    val httpServer = HttpServer.create(InetSocketAddress(0), 0)
    httpServer.setExecutor(executor.get)
    httpServer.createContext("/", echoHandler)
    httpServer.start
    server.set(httpServer)
  }

  override def afterAll() = {
    server.get.stop(1_000)
    executor.get.shutdown
  }

  val imageFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("47DegLogo", "svg").toFile(),
        (file, _) => ()
      ),
    teardown = _ => ())

  val resultFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("result", ".txt").toFile(),
        (file, _) => ()
      ),
    teardown = _ => ())

  val resultPutFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("resultPut", ".txt").toFile(),
        (file, _) => ()
      ),
    teardown = _ => ())

  val resultPatchFileResource = FunFixture(
    setup = _ =>
      Resource(
        Files.createTempFile("resultPatch", ".txt").toFile(),
        (file, _) => ()
      ),
    teardown = _ => ())

  val testBody = FunFixture(setup = _ => "test", teardown = _ => ())

  val testBodyAndFile = FunFixture.map2(testBody, resultFileResource)

  val testBodyAndPutFile =
    FunFixture.map2(testBody, resultPutFileResource)

  val testBodyAndPatchFile =
    FunFixture.map2(testBody, resultPatchFileResource)

  val file = imageFileResource

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
        val result = new String(requestInputStream.readAllBytes)
        exchange.sendResponseHeaders(200, result.length())
        outputStream.write(result.getBytes)
      } catch {
        case _ => ()
      } finally {
        exchange.close()
      }
    },
    echoStreamHandler
  )

  lazy val echoStreamHandler = HttpHandlers.handleOrElse(
    request => {
      val method = request.getRequestMethod()
      val path = request.getRequestURI().getPath()
      method match {
        case "POST" | "PATCH" | "PUT" => path.contains("stream")
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
        val result = new String(requestInputStream.readAllBytes())
        exchange.sendResponseHeaders(200, result.length())
        outputStream.write(result.getBytes())
        outputStream.flush()
      } catch {
        case _ => ()
      } finally {
        exchange.close()
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
        exchange.sendResponseHeaders(200, 0)
        getClass.getResourceAsStream("/brand.svg").transferTo(outputStream)
      } catch {
        case _ => ()
      } finally {
        exchange.close()
      }
    },
    headHandler
  )

  lazy val headHandler = HttpHandlers.handleOrElse(
    request => {
      val method = request.getRequestMethod()
      val path = request.getRequestURI().getPath()
      method match {
        case "HEAD" | "OPTIONS" | "TRACE" => true
        case _ => false
      }
    },
    (exchange: HttpExchange) => {
      try {
        val outputStream = exchange.getResponseBody()
        val requestHeaders = exchange.getRequestHeaders()
        val mutableResponseHeaders = exchange.getResponseHeaders()
        requestHeaders.forEach { (headerName, headerValues) =>
          headerValues.forEach(value => mutableResponseHeaders.add(headerName, value))
        }
        exchange.sendResponseHeaders(200, 0)
      } catch {
        case _ => ()
      } finally {
        exchange.close()
      }
    },
    timeoutHandler
  )

  lazy val timeoutHandler = HttpHandlers.handleOrElse(
    request => {
      val path = request.getRequestURI().getPath()
      path.contains("shouldTimeout")
    },
    (exchange: HttpExchange) => {
      try {
        val outputStream = exchange.getResponseBody()
        val requestHeaders = exchange.getRequestHeaders()
        val mutableResponseHeaders = exchange.getResponseHeaders()
        requestHeaders.forEach { (headerName, headerValues) =>
          headerValues.forEach(value => mutableResponseHeaders.add(headerName, value))
        }
        Thread.sleep(30000)
        exchange.sendResponseHeaders(200, 0)
        outputStream.flush()
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
