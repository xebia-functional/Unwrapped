package fx

import munit.FunSuite

import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

trait HttpServerFixtures:
  self: FunSuite =>

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

  lazy val serverProblemHeaders =
    Headers.of(
      "Content-Type",
      "text/plain; charset=UTF-8",
      "Connection",
      "close",
      "Date",
      "Fri, 01 Jul 2022 04:22:42 GMT"
    )

  lazy val patchHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "PATCH" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val traceHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "TRACE" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val optionsHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request.getRequestMethod() == "OPTIONS" && request
          .getRequestURI()
          .getPath()
          .contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val deleteHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "DELETE" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(204, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val putHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "PUT" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(204, getSuccessHeaders, ""),
      fallbackHttpHandler
    )

  lazy val headHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      request =>
        request
          .getRequestMethod() == "HEAD" && request.getRequestURI().getPath().contains("ping"),
      HttpHandlers.of(200, getSuccessHeaders, "OK"),
      fallbackHttpHandler
    )

  lazy val postHttpSuccessHandler =
    HttpHandlers.handleOrElse(
      (request: Request) => {
        request
          .getRequestMethod() == "POST" && request.getRequestURI().getPath().contains("ping")
      },
      HttpHandlers.of(201, getSuccessHeaders, "Created"),
      fallbackHttpHandler
    )

  def httpServer(handler: HttpHandler) =
    FunFixture(
      setup = _ => {
        for {
          server <- Resource(
            HttpServer.create(InetSocketAddress(0), 0),
            (server, _) => server.stop(0))
          serverExecutor <- Resource(
            Executors.newVirtualThreadPerTaskExecutor,
            (executor, _) => executor.shutdown())
          _ = server.setExecutor(serverExecutor)
          httpContext = server.createContext("/root", handler)
          _ = server.start
        } yield s"http:/${server.getAddress()}/root"
      },
      teardown = server => {
        ()
      }
    )

  def getHttpFailureHandler(
      maybeExpectedHeaders: Option[Headers],
      numRequiredFailures: AtomicInteger): HttpHandler =
    val serverFail = HttpHandlers.of(500, serverProblemHeaders, "Server Error")
    val successHandler = getHttpHandler(maybeExpectedHeaders)
    HttpHandlers.handleOrElse(
      _ => {
        numRequiredFailures.getAndDecrement > 0
      },
      serverFail,
      successHandler
    )

  lazy val fallbackHttpHandler =
    HttpHandlers.of(404, notFoundHeaders, "Not Found")

  lazy val loremIpsumBody =
    """|Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
       |tempor incididunt ut labore et dolore magna aliqua. Augue
       |neque gravida in fermentum et sollicitudin ac orci. Tristique
       |senectus et netus et malesuada fames ac. Iaculis at erat
       |pellentesque adipiscing commodo elit. Dolor sit amet
       |consectetur adipiscing elit ut aliquam. Lobortis scelerisque
       |fermentum dui faucibus in ornare quam viverra. Sodales ut
       |etiam sit amet nisl. Egestas quis ipsum suspendisse
       |ultrices. Et magnis dis parturient montes nascetur ridiculus
       |mus mauris vitae. Pellentesque adipiscing commodo elit at
       |imperdiet dui accumsan sit amet. Pellentesque pulvinar
       |pellentesque habitant morbi tristique senectus et. Semper
       |quis lectus nulla at volutpat diam ut venenatis tellus. Elit
       |ut aliquam purus sit amet luctus venenatis. Hendrerit gravida
       |rutrum quisque non tellus orci ac. Orci ac auctor augue
       |mauris.         |
       |Vestibulum lectus mauris ultrices eros. Eget nunc
       |scelerisque viverra mauris in. Odio facilisis mauris sit amet
       |massa vitae tortor condimentum lacinia. Nunc congue nisi
       |vitae suscipit tellus mauris a diam maecenas. Egestas sed sed
       |risus pretium. Congue nisi vitae suscipit tellus mauris a
       |diam maecenas. Rhoncus urna neque viverra justo nec. Vivamus
       |arcu felis bibendum ut tristique et egestas quis. Non nisi
       |est sit amet facilisis magna etiam tempor. Non arcu risus
       |quis varius quam. Eu scelerisque felis imperdiet proin
       |fermentum leo vel orci. Ipsum suspendisse ultrices gravida
       |dictum fusce. Neque viverra justo nec ultrices dui sapien
       |eget. Mattis aliquam faucibus purus in massa tempor. Arcu dui
       |vivamus arcu felis bibendum ut. Ipsum consequat nisl vel
       |pretium lectus quam id leo. Nibh cras pulvinar mattis nunc
       |sed blandit libero. Laoreet sit amet cursus sit amet dictum
       |sit amet justo. Suspendisse ultrices gravida dictum fusce. A
       |cras semper auctor neque vitae tempus quam pellentesque
       |nec.
       |
       |Molestie a iaculis at erat pellentesque. Sit amet
       |risus nullam eget felis eget nunc lobortis mattis. Vitae
       |tempus quam pellentesque nec nam aliquam sem et
       |tortor. Venenatis a condimentum vitae sapien. Diam quam nulla
       |porttitor massa id neque aliquam vestibulum morbi. Iaculis
       |urna id volutpat lacus laoreet non. Phasellus egestas tellus
       |rutrum tellus pellentesque eu. Nisl purus in mollis nunc
       |sed. Adipiscing at in tellus integer feugiat scelerisque
       |varius morbi. Penatibus et magnis dis parturient montes
       |nascetur. Id interdum velit laoreet id donec ultrices. Massa
       |sed elementum tempus egestas sed. Mus mauris vitae ultricies
       |leo integer malesuada nunc vel risus. Quis lectus nulla at
       |volutpat diam. Scelerisque purus semper eget duis at tellus
       |at urna.
       |
       |Varius morbi enim nunc faucibus a
       |pellentesque. Sed risus pretium quam vulputate dignissim
       |suspendisse in est ante. Elementum nisi quis eleifend quam
       |adipiscing vitae proin sagittis. Elementum nibh tellus
       |molestie nunc. Fermentum et sollicitudin ac orci
       |phasellus. Mi tempus imperdiet nulla malesuada
       |pellentesque. Sed velit dignissim sodales ut eu sem integer
       |vitae. Nunc consequat interdum varius sit amet mattis. Sit
       |amet aliquam id diam maecenas ultricies mi eget. In iaculis
       |nunc sed augue lacus viverra vitae congue eu. Sed viverra
       |ipsum nunc aliquet bibendum. Dolor morbi non arcu risus quis
       |varius quam quisque id. Leo vel orci porta non pulvinar
       |neque. In cursus turpis massa tincidunt dui ut. Rhoncus dolor
       |purus non enim praesent elementum facilisis leo vel. Justo
       |donec enim diam vulputate ut pharetra sit amet. Eu mi
       |bibendum neque egestas congue quisque egestas diam. Nunc
       |pulvinar sapien et ligula ullamcorper malesuada
       |proin.
       |
       |Enim ut tellus elementum sagittis vitae et leo
       |duis. Odio aenean sed adipiscing diam donec adipiscing
       |tristique risus nec. Mauris a diam maecenas sed enim
       |ut. Suscipit tellus mauris a diam maecenas. Leo vel fringilla
       |est ullamcorper eget nulla. Massa enim nec dui nunc mattis
       |enim ut. Malesuada fames ac turpis egestas maecenas pharetra
       |convallis posuere morbi. In vitae turpis massa sed elementum
       |tempus egestas sed. Accumsan lacus vel facilisis volutpat
       |est. Eget egestas purus viverra accumsan in nisl. Sit amet
       |commodo nulla facilisi nullam vehicula. Sed ullamcorper morbi
       |tincidunt ornare massa. Leo vel fringilla est ullamcorper
       |eget nulla.""".stripMargin

  def getLoremHandler(maybeExpectedHeaders: Option[Headers]) = {
    HttpHandlers.handleOrElse(
      (request: Request) => {
        val hasAllExpectedHeaders = maybeExpectedHeaders.map {
          _.entrySet.asScala.forall { entrySet =>
            val headerName = entrySet.getKey
            request.getRequestHeaders.containsKey(headerName) && request
              .getRequestHeaders
              .get(headerName)
              .containsAll(entrySet.getValue)
          }
        }
        hasAllExpectedHeaders.getOrElse(true) && request
          .getRequestMethod() == "GET" && request.getRequestURI().getPath().contains("stream")
      },
      HttpHandlers.of(200, getSuccessHeaders, loremIpsumBody),
      fallbackHttpHandler
    )
  }

  def getHttpHandler(maybeExpectedHeaders: Option[Headers]) =
    HttpHandlers.handleOrElse(
      (request: Request) => {
        val hasAllExpectedHeaders = maybeExpectedHeaders.map {
          _.entrySet.asScala.forall { entrySet =>
            val headerName = entrySet.getKey
            request.getRequestHeaders.containsKey(headerName) && request
              .getRequestHeaders
              .get(headerName)
              .containsAll(entrySet.getValue)
          }
        }
        hasAllExpectedHeaders.getOrElse(true) && request
          .getRequestMethod() == "GET" && request.getRequestURI().getPath().contains("ping")
      },
      HttpHandlers.of(200, getSuccessHeaders, "pong"),
      fallbackHttpHandler
    )

  lazy val virtualThreadExecutor =
    FunFixture(
      setup = _ => Executors.newVirtualThreadPerTaskExecutor,
      teardown = executor => {
        println("tearing down executor")
        executor.shutdown
      })
