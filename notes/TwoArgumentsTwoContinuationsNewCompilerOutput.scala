package examples {
  import continuations.Suspend
  final lazy module val TwoArgumentsTwoContinuations$package: examples.TwoArgumentsTwoContinuations$package = new examples.TwoArgumentsTwoContinuations$package()
  @SourceFile("two-arguments-two-continuations/src/main/scala/examples/TwoArgumentsTwoContinuations.scala") final module class TwoArgumentsTwoContinuations$package() extends
     
  Object() { this: examples.TwoArgumentsTwoContinuations$package.type =>
    private def writeReplace(): AnyRef = new scala.runtime.ModuleSerializationProxy(classOf[examples.TwoArgumentsTwoContinuations$package.type])
    @main def TwoArgumentsTwoContinuations: Unit = 
      {
        private class $twoArgumentsTwoContinuations$Frame($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl(
          $completion
        , $completion.context) {
          var I$0: Any = _
          var I$1: Any = _
          def I$0_=(x$0: Any): Unit = ()
          def I$1_=(x$0: Any): Unit = ()
          var $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = _
          var $label: Int = _
          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
          def $label_=(x$0: Int): Unit = ()
          protected override def invokeSuspend(result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): 
            Any | Null
           = 
            {
              this.$result = result
              this.$label = this.$label.|(scala.Int.MinValue)
              twoArgumentsTwoContinuations(null, null, this)
            }
          override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] = 
            new continuations.jvm.internal.BaseContinuationImpl(completion)
        }
        def twoArgumentsTwoContinuations(x: Int, y: Int, completion: continuations.Continuation[Int]): 
          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
         = 
          {
            var x##1: Int = x
            var y##1: Int = y
            {
              val $continuation: $twoArgumentsTwoContinuations$Frame = 
                completion match 
                  {
                    case x$0 @ x$0:$twoArgumentsTwoContinuations$Frame if x$0.$label.&(scala.Int.MinValue).!=(0) => 
                      x$0.$label = x$0.$label.-(scala.Int.MinValue)
                      x$0
                    case _ => new $twoArgumentsTwoContinuations$Frame(completion)
                  }
              $continuation.$label match 
                {
                  case 0 => 
                    continuations.Continuation.checkResult($continuation.$result)
                    $continuation.I$0 = y##1
                    $continuation.I$1 = x##1
                    $continuation.$label = 1
                    val safeContinuation: continuations.SafeContinuation[Unit] = continuations.SafeContinuation.init[Unit]($continuation)
                    {
                      {
                        safeContinuation.resume(println(x##1.+(y##1)))
                      }
                    }
                    safeContinuation.getOrThrow() match 
                      {
                        case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                        case orThrow @ <empty> => return[label1] ()
                      }
                  case 1 => 
                    y##1 = $continuation.I$0
                    x##1 = $continuation.I$1
                    continuations.Continuation.checkResult($continuation.$result)
                    label1[Unit]: <empty>
                    $continuation.I$0 = y##1
                    $continuation.I$1 = x##1
                    $continuation.$label = 2
                    val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                    {
                      {
                        safeContinuation.resume(1)
                      }
                    }
                    safeContinuation.getOrThrow() match 
                      {
                        case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                        case orThrow @ <empty> => orThrow
                      }
                  case 2 => 
                    y##1 = $continuation.I$0
                    x##1 = $continuation.I$1
                    continuations.Continuation.checkResult($continuation.$result)
                    $continuation.$result
                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
                }
            }
          }
        println(twoArgumentsTwoContinuations(1, 2)(continuations.Suspend.given_Suspend))
      }
  }
  @SourceFile("two-arguments-two-continuations/src/main/scala/examples/TwoArgumentsTwoContinuations.scala") final class TwoArgumentsTwoContinuations() extends Object() {
    <static> def main(args: Array[String]): Unit = 
      try examples.TwoArgumentsTwoContinuations$package.TwoArgumentsTwoContinuations catch 
        {
          case error @ _:scala.util.CommandLineParser.ParseError => scala.util.CommandLineParser.showError(error)
        }
  }
}
