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
                  $continuation.$label = 1
                  val safeContinuation: continuations.SafeContinuation[Boolean] = 
                    new continuations.SafeContinuation[Boolean](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
                      , 
                    continuations.Continuation.State.Undecided)
                  {
                    {
                      safeContinuation.resume(Right.apply[Nothing, Boolean](false))
                    }
                  }
                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
                    safeContinuation.getOrThrow()
                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
                  return[label1] ()
                case 1 => 
                  continuations.Continuation.checkResult($result)
                  label1[Unit]: <empty>
                  $continuation.$label = 2
                  val safeContinuation: continuations.SafeContinuation[String] = 
                    new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)(), 
                      continuations.Continuation.State.Undecided
                    )
                  {
                    {
                      safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
                    }
                  }
                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
                    safeContinuation.getOrThrow()
                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
                  return[label2] ()
                case 2 => 
                  continuations.Continuation.checkResult($result)
                  label2[Unit]: <empty>
                  $continuation.$label = 3
                  val safeContinuation: continuations.SafeContinuation[Int] = 
                    new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
                      continuations.Continuation.State.Undecided
                    )
                  {
                    {
                      safeContinuation.resume(Right.apply[Nothing, Int](1))
                    }
                  }
                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
                    safeContinuation.getOrThrow()
                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
                  orThrow
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
