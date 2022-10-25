package http4s
package fx

import _root_.fx.{defaultRetryPolicy, parallel, structured, GET, POST, Structured}
import munit.fx.ScalaFXSuite

import java.net.URI

class BlazeServerFXBackendSuite extends ScalaFXSuite, HttpServerFixtures:

  httpServerHttp4s(pingPongApp).testFX(
    "GET requests should be returned in non-blocking fibers") { serverResource =>
    serverResource.use { baseServerAddress =>
      val pongResponse: Structured ?=> (String, String, String) =
        parallel(
          () => URI.create(s"$baseServerAddress/ping/1").GET[String]().body,
          () => URI.create(s"$baseServerAddress/ping/2").GET[String]().body,
          () => URI.create(s"$baseServerAddress/ping/3").GET[String]().body
        )

      assertEqualsFX(
        structured(pongResponse),
        ("pong", "pong", "pong")
      )
    }
  }

  httpServerHttp4s(echoApp).testFX("POST requests should be returned".ignore) {
    serverResource =>
      serverResource.use { baseServerAddress =>

        val response =
          structured(URI.create(s"$baseServerAddress/echo").POST[String, String]("hello"))

        assertEqualsFX(response.body, "hello")
        assertEqualsFX(response.statusCode, 200)
      }
  }
