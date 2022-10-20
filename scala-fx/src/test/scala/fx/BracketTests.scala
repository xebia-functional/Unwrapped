package fx

import fx.ResourcesTests.property
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

import java.util.concurrent.{CompletableFuture, CompletionException}

object BracketTests extends Properties("Bracket Tests"):

  property("bracketCase identity") = forAll { (n: Int) =>
    bracketCase(() => n, identity, (_, _) => ()) == n
  }

  case class CustomEx(val token: String) extends RuntimeException

  property("bracketCase exception identity") = forAll { (msg: String) =>
    val res =
      try
        bracketCase(
          () => throw CustomEx(msg),
          _ => throw RuntimeException("Cannot come here"),
          (_, _) => ())
      catch case CustomEx(msg) => msg

    res == msg
  }

  property("bracketCase must run release task on use error") = forAll { (msg: String) =>
    val promise = new CompletableFuture[ExitCase]
    val res =
      try bracketCase(() => (), _ => throw CustomEx(msg), (_, ex) => promise.complete(ex))
      catch case CustomEx(msg) => msg

    res == msg && promise.join() == ExitCase.Failure(CustomEx(msg))
  }

  property("bracketCase must run release task on use success") = forAll { (msg: String) =>
    val promise = new CompletableFuture[ExitCase]
    val res = bracketCase(() => (), _ => msg, (_, ex) => promise.complete(ex))

    res == msg && promise.join() == ExitCase.Completed
  }

  property("bracketCase cancellation in use") = forAll { (msg: String) =>
    val latch = new CompletableFuture[Unit]
    val promise = new CompletableFuture[ExitCase]
    structured {
      val fiber = fork(() =>
        bracketCase(
          () => (),
          _ => {
            latch.complete(())
            Thread.sleep(100_000)
          },
          (_, ex) => promise.complete(ex)))
      latch.join()
      fiber.cancel()
      promise.join().isInstanceOf[ExitCase.Cancelled]
    }
  }

end BracketTests
