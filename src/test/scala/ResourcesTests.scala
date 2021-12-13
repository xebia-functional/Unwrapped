package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import java.util.concurrent.CompletableFuture
import scala.util.control.NonFatal
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException

object ResourcesTests extends Properties("Resources Tests"):

  property("Can consume resource") = forAll { (n: Int) =>
    val r = Resource(n, (_, _) => ())
    r.use(_ + 1) == n + 1
  }

  property("value resource is released with Complete") = forAll { (n: Int) =>
    val p = CompletableFuture[ExitCase]()
    val r = Resource(n, (_, ex) => require(p.complete(ex)))
    r.use((_) => ())
    p.join == ExitCase.Completed
  }

  case class CustomEx(val token: String) extends RuntimeException

  property("error resource finishes with error") = forAll { (n: String) =>
    val p = CompletableFuture[ExitCase]()
    val r = Resource[Int](throw CustomEx(n), (_, ex) => require(p.complete(ex)))
    val result =
      try
        r.use(_ + 1)
        "unexpected"
      catch
        case e: ExecutionException => e.getCause.asInstanceOf[CustomEx].token
    result == n
  }

end ResourcesTests

/*
val p = CompletableDeferred<ExitCase>()
        val r = Resource<Int>({ throw e }, { _, ex -> require(p.complete(ex)) })

        Either.catch {
          r.use { it + 1 }
        } should leftException(e)
 */
