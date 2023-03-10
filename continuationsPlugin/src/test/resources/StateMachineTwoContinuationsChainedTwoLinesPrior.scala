package continuations {
  final lazy module val compileFromString$package:
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    class compileFromString$package$fooTest$1($completion: continuations.Continuation[Any | Null]) extends
      continuations.jvm.internal.ContinuationImpl
    ($completion, $completion.context) {
      var I$0: Any = _
      var I$1: Any = _
      var I$2: Any = _
      def I$0_=(x$0: Any): Unit = ()
      def I$1_=(x$0: Any): Unit = ()
      def I$2_=(x$0: Any): Unit = ()
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
          continuations.compileFromString$package.fooTest(null, null, this)
        }
    }
    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
     =
      {
        var x##1: Int = x
        var y##1: Int = y
        var z: Int = null
        {
          val $continuation:
            continuations.compileFromString$package.
              compileFromString$package$fooTest$1
           =
            completion match
              {
                case x$0 @ <empty> if
                  x$0.isInstanceOf[
                    continuations.compileFromString$package.
                      compileFromString$package$fooTest$1
                  ].&&(
                    x$0.asInstanceOf[
                      continuations.compileFromString$package.
                        compileFromString$package$fooTest$1
                    ].$label.&(scala.Int.MinValue).!=(0)
                  )
                 =>
                  x$0.asInstanceOf[
                    continuations.compileFromString$package.
                      compileFromString$package$fooTest$1
                  ].$label =
                    x$0.asInstanceOf[
                      continuations.compileFromString$package.
                        compileFromString$package$fooTest$1
                    ].$label.-(scala.Int.MinValue)
                  x$0
                case _ =>
                  new
                    continuations.compileFromString$package.
                      compileFromString$package$fooTest$1
                  (completion)
              }
          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
            $continuation.$result
          $continuation.$label match
            {
              case 0 =>
                continuations.Continuation.checkResult($result)
                println("Hello")
                println("World")
                $continuation.I$0 = y##1
                $continuation.I$1 = x##1
                $continuation.$label = 1
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
                }
                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
                  safeContinuation.getOrThrow()
                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
                z = orThrow.asInstanceOf[Int]
                return[label1] ()
              case 1 =>
                y##1 = $continuation.I$0
                x##1 = $continuation.I$1
                continuations.Continuation.checkResult($result)
                z = $result.asInstanceOf[Int]
                label1[Unit]: <empty>
                $continuation.I$0 = y##1
                $continuation.I$1 = x##1
                $continuation.I$2 = z
                $continuation.$label = 2
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
                }
                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
                  safeContinuation.getOrThrow()
                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
                orThrow
              case 2 =>
                y##1 = $continuation.I$0
                x##1 = $continuation.I$1
                z = $continuation.I$2
                continuations.Continuation.checkResult($result)
                $result
              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
            }
        }
      }
  }
}
