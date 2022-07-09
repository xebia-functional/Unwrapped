package fx

import munit.fx.ScalaFXSuite
import java.net.http.HttpResponse.ResponseInfo

import java.net.http.HttpResponse
import java.net.http.HttpHeaders
import java.net.http.HttpClient.Version
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.Executors
import java.nio.charset.StandardCharsets

import scala.jdk.CollectionConverters.*

class HttpResponseMapperSuite extends ScalaFXSuite {
  testFX("the given receive[byte] should return an fx.Receive[Byte]") {
    val mapper: Structured ?=> HttpResponseMapper[Receive[Byte]] =
      HttpResponseMapper.HttpResponseReceiveMapper

    val bodyHandler: Structured ?=> HttpResponse.BodySubscriber[fx.Receive[Byte]] =
      mapper.bodyHandler(new ResponseInfo {
        def headers(): HttpHeaders = ???
        def statusCode(): Int = 200
        def version(): Version = ???
      })

    val publisher = SubmissionPublisher[java.util.List[ByteBuffer]](Executors.newVirtualThreadPerTaskExecutor, 2)
    val subscribed: Structured ?=> Unit = publisher.subscribe(bodyHandler)
    val expectedBody =
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
         |mauris.
         |
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

    val splitExpectedBody =
      expectedBody.split("\n\n").foldLeft(Array.empty[String]) { (arr, s) => arr :+ s"$s\n\n" }

    val splitIntoSentences =
      (a: String) => a.split(".").foldLeft(Array.empty[String]) { (arr, s) => arr :+ s"$s." }
    val splitIntoWords = (a: Array[String]) =>
      a.flatMap(_.split(" ").foldLeft(Array.empty[String]) { (arr, s) => arr :+ s"$s " })
    val mapToByteBuffers =
      (a: Array[String]) =>
        a.foldLeft(Array.empty[ByteBuffer]) { (arr, s) =>
          arr :+ ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8))
        }

    for
      listOfByteBuffers: java.util.List[ByteBuffer] <- List(
        mapToByteBuffers(splitIntoWords(splitIntoSentences(splitExpectedBody(0))))
          .toList
          .asJava,
        mapToByteBuffers(splitIntoWords(splitIntoSentences(splitExpectedBody(1))))
          .toList
          .asJava,
        mapToByteBuffers(splitIntoWords(splitIntoSentences(splitExpectedBody(2))))
          .toList
          .asJava,
        mapToByteBuffers(splitIntoWords(splitIntoSentences(splitExpectedBody(3))))
          .toList
          .asJava,
        mapToByteBuffers(splitIntoWords(splitIntoSentences(splitExpectedBody(4)))).toList.asJava
      )
    yield publisher.offer(listOfByteBuffers, (item, subscriber) => true)

    val result = structured(bodyHandler.getBody().toCompletableFuture().join())
      .fold(Array.emptyByteArray, (arr, byte) => arr :+ byte)
      .transform[String]((bytes: Array[Byte]) =>
        send(new String(bytes, Charset.forName("UTF-8"))))
      .toList
      .mkString("")

    assertEqualsFX(result.head, expectedBody)
  }
}
