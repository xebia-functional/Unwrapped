package http4s
package fx

import _root_.fx.{defaultRetryPolicy, parallel, structured, GET, POST, Structured}
import munit.fx.ScalaFXSuite

import java.net.URI
import java.net.http.HttpResponse

class BlazeServerFXBackendSuite extends ScalaFXSuite, HttpServerFixtures:

  httpServerHttp4s(pingPongApp).testFX(
    "GET requests should be returned in non-blocking fibers") { serverResource =>
    assertEqualsFX(
      structured {
        serverResource.use { baseServerAddress =>
          parallel(
            () => URI.create(s"$baseServerAddress/ping/1").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/2").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/3").GET[String]().body
          )
        }
      },
      ("pong", "pong", "pong")
    )
  }

  httpServerHttp4s(echoApp).testFX("POST requests should be returned") { serverResource =>
    val response: HttpResponse[String] = structured {
      serverResource.use { baseServerAddress =>
        URI.create(s"$baseServerAddress/echo").POST[String, String]("hello")
      }
    }

    assertEqualsFX(response.body, "hello")
    assertEqualsFX(response.statusCode, 200)
  }
