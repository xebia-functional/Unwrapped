package unwrapped

import com.sun.net.httpserver.*
import munit.unwrapped.UnwrappedSuite

import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class HttpSuite extends UnwrappedSuite, HttpServerFixtures:

  httpServer(getHttpHandler(Option.empty))
    .testUnwrapped("requests should be returned in non-blocking fibers") { serverResource =>
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

        assertEqualsUnwrapped(
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
    .testUnwrapped("Expected headers are sent with requests, 404 means headers not sent") {
      (serverAddressResource: Resource[String]) =>
        serverAddressResource.use { baseServerAddress =>
          assertEqualsUnwrapped(
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
    .testUnwrapped("By default, failed requests should retry 3 times") {
      serverAddressResource =>
        serverAddressResource.use { baseServerAddress =>
          assertEqualsUnwrapped(
            structured(URI.create(s"$baseServerAddress/ping/fail").GET[String]().statusCode),
            200)
        }
    }

  httpServer(getHttpFailureHandler(Option.empty, AtomicInteger(4))).testUnwrapped(
    "More than three request failures should return the server error by default") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsUnwrapped(
          structured(URI.create(s"$baseServerAddress/ping/fail").GET[String]().statusCode),
          500)
      }
  }

  httpServer(postHttpSuccessHandler).testUnwrapped(
    "requests with string post bodies are successuful") { serverAddressResource =>
    serverAddressResource.use { baseServeraddress =>
      assertEqualsUnwrapped(
        structured(
          URI.create(s"$baseServeraddress/ping").POST[String, String]("paddle")).statusCode,
        201)
    }
  }

  httpServer(headHttpSuccessHandler).testUnwrapped("Head should send a HEAD request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsUnwrapped(
          structured(URI.create(s"$baseServerAddress/ping").HEAD()).statusCode,
          200
        )
      }
  }

  httpServer(putHttpSuccessHandler).testUnwrapped("PUT should send a PUT request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsUnwrapped(
          structured(
            URI.create(s"$baseServerAddress/ping").PUT[String, String]("paddle")).statusCode,
          204
        )
      }
  }

  httpServer(deleteHttpSuccessHandler).testUnwrapped("DELETE should send a DELETE request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsUnwrapped(
          structured(URI.create(s"$baseServerAddress/ping").DELETE[String]()).statusCode,
          204)
      }
  }

  httpServer(optionsHttpSuccessHandler).testUnwrapped(
    "OPTIONS should send an Options request") { serverAddressResource =>
    serverAddressResource.use { baseServerAddress =>
      assertEqualsUnwrapped(
        structured(URI.create(s"$baseServerAddress/ping").OPTIONS[String]().statusCode),
        200)
    }
  }

  httpServer(traceHttpSuccessHandler).testUnwrapped("TRACE should send a trace request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsUnwrapped(
          structured(URI.create(s"$baseServerAddress/ping").TRACE[String]().statusCode),
          200)
      }
  }

  httpServer(patchHttpSuccessHandler).testUnwrapped("PATCH should send a patch request") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        assertEqualsUnwrapped(
          structured(
            URI.create(s"$baseServerAddress/ping").PATCH[String, String]("paddle").statusCode),
          200)
      }
  }
