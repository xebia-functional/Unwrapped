package fx

import com.sun.net.httpserver.*
import munit.fx.ScalaFXSuite

import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class HttpSuite extends ScalaFXSuite, HttpServerFixtures:

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
            () => URI.create(s"$baseServerAddress/ping/1").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/2").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/3").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/4").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/5").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/6").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/7").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/8").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/9").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/0").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/11").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/12").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/13").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/14").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/15").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/16").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/17").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/18").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/19").GET[String]().body,
            () => URI.create(s"$baseServerAddress/ping/20").GET[String]().body
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
              URI
                .create(s"$baseServerAddress/ping/1")
                .GET[String](HttpHeader("X-Extended-Test-Header", "HttpSuite"))
                .body),
            "pong"
          )
        }
    }

  httpServer(getHttpFailureHandler(Option.empty, AtomicInteger(3)))
    .testFX("By default, failed requests should retry 3 times") { serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(URI.create(s"$baseServerAddress/ping/fail").GET[String]().statusCode),
          200)
      }
    }

  httpServer(getHttpFailureHandler(Option.empty, AtomicInteger(4)))
    .testFX("More than three request failures should return the server error by default") {
      serverAddressResource =>
        serverAddressResource.use { baseServerAddress =>
          assertEqualsFX(
            structured(URI.create(s"$baseServerAddress/ping/fail").GET[String]().statusCode),
            500)
        }
    }

  httpServer(postHttpSuccessHandler).testFX(
    "requests with string post bodies are successuful") { serverAddressResource =>
    serverAddressResource.use { baseServeraddress =>
      assertEqualsFX(
        structured(
          URI.create(s"$baseServeraddress/ping").POST[String, String]("paddle")).statusCode,
        201)
    }
  }

  httpServer(headHttpSuccessHandler).testFX("Head should send a HEAD request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(URI.create(s"$baseServerAddress/ping").HEAD()).statusCode,
          200
        )
      }
  }

  httpServer(putHttpSuccessHandler).testFX("PUT should send a PUT request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(
            URI.create(s"$baseServerAddress/ping").PUT[String, String]("paddle")).statusCode,
          204
        )
      }
  }

  httpServer(deleteHttpSuccessHandler).testFX("DELETE should send a DELETE request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(URI.create(s"$baseServerAddress/ping").DELETE[String]()).statusCode,
          204)
      }
  }

  httpServer(optionsHttpSuccessHandler).testFX("OPTIONS should send an Options request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(URI.create(s"$baseServerAddress/ping").OPTIONS[String]().statusCode),
          200)
      }
  }

  httpServer(traceHttpSuccessHandler).testFX("TRACE should send a trace request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(URI.create(s"$baseServerAddress/ping").TRACE[String]().statusCode),
          200)
      }
  }

  httpServer(patchHttpSuccessHandler).testFX("PATCH should send a patch request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsFX(
          structured(
            URI.create(s"$baseServerAddress/ping").PATCH[String, String]("paddle")
              .statusCode),
          200)
      }
  }
