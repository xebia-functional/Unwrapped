package http4s
package fx

import _root_.fx.handle
import _root_.fx.instances.StructuredF
import _root_.fx.instances.FxAsync.asyncInstance
import _root_.fx.{Control, Errors, Nullable, Resource, Resources, Structured}
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{defaults, Server}

class BlazeServerFXBackend private (
    app: HttpApp[StructuredF]
)(
    using structured: Structured,
    control: Control[Throwable],
    config: HttpServerConfig
) {
  private var serverFinalizers: Nullable[(Server, StructuredF[Unit])] = Nullable.none

  private val builder: BlazeServerBuilder[StructuredF] =
    BlazeServerBuilder[StructuredF]
      .bindHttp(
        config.port.getOrElse(defaults.HttpPort),
        config.host.getOrElse(defaults.IPv4Host))
      .withIdleTimeout(config.idleTimeout.getOrElse(defaults.IdleTimeout))
      .withSelectorThreadFactory(structured.threadFactory)
      .withHttpApp(app)

  def start(): BlazeServerFXBackend =
    serverFinalizers = handle(
      Nullable(builder.resource.allocated)
    )((e: Throwable) => e.shift)
    this

  def close(): Unit =
    serverFinalizers.map { case (_, finalizers) => finalizers }.getOrElse(())

  def server(): Server =
    handle(
      serverFinalizers.map { case (server, _) => server }.value
    )((e: Throwable) => e.shift)
}

object BlazeServerFXBackend:
  def apply(app: HttpApp[StructuredF])(
      using structured: Structured,
      control: Control[Throwable],
      config: HttpServerConfig
  ): Resource[BlazeServerFXBackend] =
    Resource(new BlazeServerFXBackend(app).start(), (server, _) => server.close())
