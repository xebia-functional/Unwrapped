package http4s
package fx

import _root_.fx.Nullable

import java.net.InetSocketAddress
import scala.concurrent.duration.Duration

/**
 * Defines the server configuration options for an http server. Not open for extension.
 */
final class HttpServerConfig(
    val port: Nullable[Int],
    val host: Nullable[String],
    val idleTimeout: Nullable[Duration]
)

object HttpServerConfig:
  given HttpServerConfig =
    HttpServerConfig(Nullable.none, Nullable.none, Nullable.none)
