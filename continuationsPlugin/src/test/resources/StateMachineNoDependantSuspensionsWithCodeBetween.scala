package continuations {
  final lazy module val compileFromString$package: 
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class 
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef = 
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    def program: Int = 
      {
        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
          $completion.context
        ) {
          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
          var $label: Int = _
          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
             = 
          ()
          def $label_=(x$0: Int): Unit = ()
          protected override def invokeSuspend(
            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
          ): Any | Null = 
            {
              this.$result = result
              this.$label = this.$label.|(scala.Int.MinValue)
              foo(this)
            }
          override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] =
            new continuations.jvm.internal.BaseContinuationImpl(completion)
          protected def invoke(p1: Any | Null, p2: continuations.Continuation[Any | Null]): Any | Null =
            this.create(p1, p2).asInstanceOf[(BaseContinuationImpl.this : continuations.jvm.internal.BaseContinuationImpl)].invokeSuspend(
              new Right[Unit, Unit](())
            )
        }
        def foo(completion: continuations.Continuation[Int]): 
          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
         = 
          {
            val $continuation: program$foo$1 = 
              completion match 
                {
                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
                    x$0.asInstanceOf[program$foo$1].$label = x$0.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
                    x$0
                  case _ => new program$foo$1(completion)
                }
            val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
              $continuation.$result
            $continuation.$label match 
              {
                case 0 => 
                  continuations.Continuation.checkResult($result)
                  println("Start")
                  val x: Int = 1
                  $continuation.$label = 1
                  val safeContinuation: continuations.SafeContinuation[Boolean] = continuations.SafeContinuation.init[Boolean]($continuation)
                  {
                    {
                      safeContinuation.resume(false)
                    }
                  }
                  safeContinuation.getOrThrow() match
                    {
                      case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                      case orThrow @ <empty> => return[label1] ()
                    }
                case 1 => 
                  continuations.Continuation.checkResult($result)
                  label1[Unit]: <empty>
                  println("Hello")
                  $continuation.$label = 2
                  val safeContinuation: continuations.SafeContinuation[String] = continuations.SafeContinuation.init[String]($continuation)
                  {
                    {
                      safeContinuation.resume("Hello")
                    }
                  }
                  safeContinuation.getOrThrow() match
                    {
                      case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                      case orThrow @ <empty> => return[label2] ()
                    }
                case 2 => 
                  continuations.Continuation.checkResult($result)
                  label2[Unit]: <empty>
                  $continuation.$label = 3
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
                case 3 => 
                  continuations.Continuation.checkResult($result)
                  $result
                case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
              }
          }
        foo(continuations.jvm.internal.ContinuationStub.contImpl)
      }
  }
}
