package fx

import munit.fx.DisciplineFXSuite
import _root_.cats.effect.laws.AsyncLaws
import _root_.fx.instances.{FxAsync, StructuredF}
import _root_.fx.Structured
import _root_.fx.structured
import _root_.cats.implicits._
import _root_.cats.effect.laws.AsyncTests
import _root_.cats.effect.implicits._
import org.scalacheck.{Gen}
import _root_.cats.effect.laws.AsyncTests
import _root_.cats.effect.kernel.instances.*
import _root_.cats.effect.kernel.testkit.SyncTypeGenerators.arbitrarySyncType
import _root_.cats.laws.discipline.arbitrary._
import fx.Structured
import org.scalacheck.{
Arbitrary => Arb
}
import _root_.cats.effect.IO
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class StructurredFLaws extends DisciplineFXSuite {

  import FxAsync.{given, *}

  given arbExecutionContext(using Resources, resource:Arb[Resource[ExecutionContext]]): Arb[Resource[ExecutionContext]] =
    val shutdown: (ExecutionContext, ExitCase) => Unit = (ec, exitCase) => ec.shutdown()
    for {
      threads <- Gen.choose(1, Runtime.getRuntime.availableProcessors - 1)
      rec <- Gen.oneOf(
        Resource(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()), shutdown),
        Resource(ExecutionContext.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor()), shutdown),
        Resource(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(threads)), shutdown),
        Resource(ExecutionContext.fromExecutorService(Executors.newWorkStealingPool()), shutdown)
      )

    } yield rec
  
  given arbInt[A](using arbA: Arb[A], struct: Structured): Arb[StructuredF[A]] = Arb.apply{
    
    for{
      i <- arbA.arbitrary
      f = {
        val f: Structured ?=> A = i
        f
      }
    } yield f

  }

  checkAllLaws("StructuredF.AsyncLaws"){
    structured{
      AsyncTests[StructuredF].async[Int, Int, String]
      // AsyncTests[IO].async[Int, Int, String]
    }

  }


  // checkAllLaws[Throwable](
  //   "StructuredF.AsyncLaws"
  // )(
  //   summon
  // ){
  //   structured{
  //     AsyncTests[StructuredF].async[Int, Int, String]
  //   }
  // }


    

}
