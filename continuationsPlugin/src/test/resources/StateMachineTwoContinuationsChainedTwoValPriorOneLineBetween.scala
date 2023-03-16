package continuations {

  final lazy module val compileFromString$package: continuations.compileFromString$package =
    new continuations.compileFromString$package()

  @SourceFile("compileFromString.scala") final module class compileFromString$package()
      extends Object() { this: continuations.compileFromString$package.type =>

    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])

    class compileFromString$package$fooTest$1($completion: continuations.Continuation[Any | Null])
        extends continuations.jvm.internal.ContinuationImpl($completion, $completion.context) { selfFrame =>

      var I$0: Any = _
      var I$1: Any = _
      var I$2: Any = _
      var I$3: Any = _
      def I$0_=(x$0: Any): Unit = ()
      def I$1_=(x$0: Any): Unit = ()
      def I$2_=(x$0: Any): Unit = ()
      def I$3_=(x$0: Any): Unit = ()

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
          continuations.compileFromString$package.fooTest(null, null)
        }

      override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] =
        new continuations.jvm.internal.BaseContinuationImpl(completion)
      protected def invoke(p1: Any | Null, p2: continuations.Continuation[Any | Null]): Any | Null =
        this.create(p1, p2).asInstanceOf[(BaseContinuationImpl.this : continuations.jvm.internal.BaseContinuationImpl)].invokeSuspend(
          new Right[Unit, Unit](())
        )

      def fooTest(x: Int, y: Int):
          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      = {
        var x##1: Int = x
        var y##1: Int = y
        var a: Int = null
        var z: Int = null
        {
          $label match
            {
              case 0 =>
                continuations.Continuation.checkResult($result)
                a = 1
                val w: Int = 1
                I$0 = y##1
                I$1 = x##1
                I$2 = a
                $label = 1
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](selfFrame)
                {
                  safeContinuation.resume(x##1.+(y##1).+(w))
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> =>
                      z = orThrow.asInstanceOf[Int]
                      return[label1] ()
                  }
              case 1 =>
                y##1 = I$0
                x##1 = I$1
                a = I$2
                continuations.Continuation.checkResult($result)
                z = $result.asInstanceOf[Int]
                label1[Unit]: <empty>
                println("Hello")
                I$0 = y##1
                I$1 = x##1
                I$2 = a
                I$3 = z
                $label = 2
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](selfFrame)
                {
                  safeContinuation.resume(z.+(a))
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> => orThrow
                  }
              case 2 =>
                y##1 = I$0
                x##1 = I$1
                a = I$2
                z = I$3
                continuations.Continuation.checkResult($result)
                $result
              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
            }
        }
      }
    }
  }
}
