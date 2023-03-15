package continuations {
  final lazy module val compileFromString$package:
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    class compileFromString$package$foo$1($completion: continuations.Continuation[Any | Null]) extends
      continuations.jvm.internal.ContinuationImpl
    ($completion, $completion.context) {
      var I$0: Any = _
      var I$1: Any = _
      def I$0_=(x$0: Any): Unit = ()
      def I$1_=(x$0: Any): Unit = ()
      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      var $label: Int = _
      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
      def $label_=(x$0: Int): Unit = ()
      protected override def invokeSuspend(
        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
      ): Any | Null =
        {
          this.$result = result
          this.$label = this.$label.|(scala.Int.MinValue)
          continuations.compileFromString$package.foo(null, this)
        }
      override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] =
        new continuations.jvm.internal.BaseContinuationImpl(completion)
      protected def invoke(p1: Any | Null, p2: continuations.Continuation[Any | Null]): Any | Null =
        this.create(p1, p2).asInstanceOf[(BaseContinuationImpl.this : continuations.jvm.internal.BaseContinuationImpl)].invokeSuspend(
          new Right[Unit, Unit](())
        )
    }
    def foo(x: Int, completion: continuations.Continuation[Int]):
      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
     =
      {
        var x##1: Int = x
        var y: Int = null
        {
          val $continuation:
            continuations.compileFromString$package.
              compileFromString$package$foo$1
           =
            completion match
              {
                case x$0 @ <empty> if
                  x$0.isInstanceOf[
                    continuations.compileFromString$package.
                      compileFromString$package$foo$1
                  ].&&(
                    x$0.asInstanceOf[
                      continuations.compileFromString$package.
                        compileFromString$package$foo$1
                    ].$label.&(scala.Int.MinValue).!=(0)
                  )
                 =>
                  x$0.asInstanceOf[
                    continuations.compileFromString$package.
                      compileFromString$package$foo$1
                  ].$label =
                    x$0.asInstanceOf[
                      continuations.compileFromString$package.
                        compileFromString$package$foo$1
                    ].$label.-(scala.Int.MinValue)
                  x$0
                case _ =>
                  new
                    continuations.compileFromString$package.
                      compileFromString$package$foo$1
                  (completion)
              }
          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
            $continuation.$result
          $continuation.$label match
            {
              case 0 =>
                continuations.Continuation.checkResult($result)
                $continuation.I$0 = x##1
                $continuation.$label = 1
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  {
                    safeContinuation.resume(1)
                  }
                }
                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
                  safeContinuation.getOrThrow()
                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
                y = orThrow.asInstanceOf[Int]
              case 1 =>
                x##1 = $continuation.I$0
                continuations.Continuation.checkResult($result)
                y = $result.asInstanceOf[Int]
              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
            }
        }
        x##1.+(y)
      }
  }
}
