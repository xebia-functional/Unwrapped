package continuations

trait ContinuationsPluginFixtures {
  // format: off
  val expectedStateMachineOneParamOneDependantContinuation =
      """|
         |package continuations {
         |  final lazy module val compileFromString$package:
         |    continuations.compileFromString$package
         |   = new continuations.compileFromString$package()
         |  @SourceFile("compileFromString.scala") final module class
         |    compileFromString$package
         |  () extends Object() { this: continuations.compileFromString$package.type =>
         |    private def writeReplace(): AnyRef =
         |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
         |    class compileFromString$package$foo$1($completion: continuations.Continuation[Any | Null]) extends
         |      continuations.jvm.internal.ContinuationImpl
         |    ($completion, $completion.context) {
         |      var I$0: Any = _
         |      var I$1: Any = _
         |      def I$0_=(x$0: Any): Unit = ()
         |      def I$1_=(x$0: Any): Unit = ()
         |      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
         |      var $label: Int = _
         |      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
         |      def $label_=(x$0: Int): Unit = ()
         |      protected override def invokeSuspend(
         |        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
         |      ): Any | Null =
         |        {
         |          this.$result = result
         |          this.$label = this.$label.|(scala.Int.MinValue)
         |          continuations.compileFromString$package.foo(null, this.asInstanceOf[continuations.Continuation[Int]])
         |        }
         |    }
         |    def foo(x: Int, completion: continuations.Continuation[Int]):
         |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
         |     =
         |      {
         |        var x##1: Int = x
         |        var y: Int = null
         |        {
         |          var $continuation: continuations.Continuation[Any] | Null = null
         |          completion match
         |            {
         |              case x$0 @ <empty> if
         |                x$0.isInstanceOf[
         |                  continuations.compileFromString$package.
         |                    compileFromString$package$foo$1
         |                ].&&(
         |                  x$0.asInstanceOf[
         |                    continuations.compileFromString$package.
         |                      compileFromString$package$foo$1
         |                  ].$label.&(scala.Int.MinValue).!=(0)
         |                )
         |               =>
         |                $continuation =
         |                  x$0.asInstanceOf[
         |                    continuations.compileFromString$package.
         |                      compileFromString$package$foo$1
         |                  ]
         |                $continuation.asInstanceOf[
         |                  continuations.compileFromString$package.
         |                    compileFromString$package$foo$1
         |                ].$label =
         |                  $continuation.asInstanceOf[
         |                    continuations.compileFromString$package.
         |                      compileFromString$package$foo$1
         |                  ].$label.-(scala.Int.MinValue)
         |              case _ =>
         |                $continuation =
         |                  new
         |                    continuations.compileFromString$package.
         |                      compileFromString$package$foo$1
         |                  (completion.asInstanceOf[continuations.Continuation[Any | Null]])
         |            }
         |          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
         |            $continuation.asInstanceOf[
         |              continuations.compileFromString$package.
         |                compileFromString$package$foo$1
         |            ].$result
         |          $continuation.asInstanceOf[
         |            continuations.compileFromString$package.
         |              compileFromString$package$foo$1
         |          ].$label match
         |            {
         |              case 0 =>
         |                if $result.!=(null) then
         |                  $result.fold[Unit](
         |                    {
         |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
         |                      closure($anonfun)
         |                    }
         |                  ,
         |                    {
         |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
         |                      closure($anonfun)
         |                    }
         |                  )
         |                 else ()
         |                $continuation.asInstanceOf[
         |                  continuations.compileFromString$package.
         |                    compileFromString$package$foo$1
         |                ].I$0 = x##1
         |                $continuation.asInstanceOf[
         |                  continuations.compileFromString$package.
         |                    compileFromString$package$foo$1
         |                ].$label = 1
         |                val safeContinuation: continuations.SafeContinuation[Int] =
         |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
         |                    continuations.Continuation.State.Undecided
         |                  )
         |                safeContinuation.resume(Right.apply[Nothing, Int](1))
         |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
         |                  safeContinuation.getOrThrow()
         |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
         |                y = orThrow.asInstanceOf[Int]
         |                ()
         |              case 1 =>
         |                x##1 =
         |                  $continuation.asInstanceOf[
         |                    continuations.compileFromString$package.
         |                      compileFromString$package$foo$1
         |                  ].I$0
         |                if $result.!=(null) then
         |                  $result.fold[Unit](
         |                    {
         |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
         |                      closure($anonfun)
         |                    }
         |                  ,
         |                    {
         |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
         |                      closure($anonfun)
         |                    }
         |                  )
         |                 else ()
         |                y = $result.asInstanceOf[Int]
         |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
         |            }
         |        }
         |        x##1.+(y)
         |      }
         |  }
         |}
         """.stripMargin
  // format: on

  val expectedStateMachineOneParamOneNoDependantContinuation =
    """
      |package continuations {
      |  final lazy module val compileFromString$package:
      |    continuations.compileFromString$package
      |   = new continuations.compileFromString$package()
      |  @SourceFile("compileFromString.scala") final module class
      |    compileFromString$package
      |  () extends Object() { this: continuations.compileFromString$package.type =>
      |    private def writeReplace(): AnyRef =
      |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
      |    class compileFromString$package$foo$1($completion: continuations.Continuation[Any | Null]) extends
      |      continuations.jvm.internal.ContinuationImpl
      |    ($completion, $completion.context) {
      |      var I$0: Any = _
      |      def I$0_=(x$0: Any): Unit = ()
      |      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      |      var $label: Int = _
      |      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
      |      def $label_=(x$0: Int): Unit = ()
      |      protected override def invokeSuspend(
      |        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
      |      ): Any | Null =
      |        {
      |          this.$result = result
      |          this.$label = this.$label.|(scala.Int.MinValue)
      |          continuations.compileFromString$package.foo(null, this.asInstanceOf[continuations.Continuation[Int]])
      |        }
      |    }
      |    def foo(qq: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var qq##1: Int = qq
      |        {
      |          var $continuation: continuations.Continuation[Any] | Null = null
      |          completion match
      |            {
      |              case x$0 @ <empty> if
      |                x$0.isInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$foo$1
      |                ].&&(
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$foo$1
      |                  ].$label.&(scala.Int.MinValue).!=(0)
      |                )
      |               =>
      |                $continuation =
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$foo$1
      |                  ]
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$foo$1
      |                ].$label =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$foo$1
      |                  ].$label.-(scala.Int.MinValue)
      |              case _ =>
      |                $continuation =
      |                  new
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$foo$1
      |                  (completion.asInstanceOf[continuations.Continuation[Any | Null]])
      |            }
      |          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
      |            $continuation.asInstanceOf[
      |              continuations.compileFromString$package.
      |                compileFromString$package$foo$1
      |            ].$result
      |          $continuation.asInstanceOf[
      |            continuations.compileFromString$package.
      |              compileFromString$package$foo$1
      |          ].$label match
      |            {
      |              case 0 =>
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$foo$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$foo$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Unit] =
      |                  new continuations.SafeContinuation[Unit](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Unit]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, Unit](
      |                    {
      |                      println(qq##1)
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                ()
      |              case 1 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$foo$1
      |                  ].I$0
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                ()
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        10
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineNoParamOneNoDependantContinuationCodeBeforeUsedAfter =
      """
        |package continuations {
        |  final lazy module val compileFromString$package:
        |    continuations.compileFromString$package
        |   = new continuations.compileFromString$package()
        |  @SourceFile("compileFromString.scala") final module class
        |    compileFromString$package
        |  () extends Object() { this: continuations.compileFromString$package.type =>
        |    private def writeReplace(): AnyRef =
        |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
        |    class compileFromString$package$foo$1($completion: continuations.Continuation[Any | Null]) extends
        |      continuations.jvm.internal.ContinuationImpl
        |    ($completion, $completion.context) {
        |      var I$0: Any = _
        |      def I$0_=(x$0: Any): Unit = ()
        |      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
        |      var $label: Int = _
        |      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
        |      def $label_=(x$0: Int): Unit = ()
        |      protected override def invokeSuspend(
        |        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
        |      ): Any | Null =
        |        {
        |          this.$result = result
        |          this.$label = this.$label.|(scala.Int.MinValue)
        |          continuations.compileFromString$package.foo(this.asInstanceOf[continuations.Continuation[Int]])
        |        }
        |    }
        |    def foo(completion: continuations.Continuation[Int]):
        |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
        |     =
        |      {
        |        var xx: Int = null
        |        {
        |          var $continuation: continuations.Continuation[Any] | Null = null
        |          completion match
        |            {
        |              case x$0 @ <empty> if
        |                x$0.isInstanceOf[
        |                  continuations.compileFromString$package.
        |                    compileFromString$package$foo$1
        |                ].&&(
        |                  x$0.asInstanceOf[
        |                    continuations.compileFromString$package.
        |                      compileFromString$package$foo$1
        |                  ].$label.&(scala.Int.MinValue).!=(0)
        |                )
        |               =>
        |                $continuation =
        |                  x$0.asInstanceOf[
        |                    continuations.compileFromString$package.
        |                      compileFromString$package$foo$1
        |                  ]
        |                $continuation.asInstanceOf[
        |                  continuations.compileFromString$package.
        |                    compileFromString$package$foo$1
        |                ].$label =
        |                  $continuation.asInstanceOf[
        |                    continuations.compileFromString$package.
        |                      compileFromString$package$foo$1
        |                  ].$label.-(scala.Int.MinValue)
        |              case _ =>
        |                $continuation =
        |                  new
        |                    continuations.compileFromString$package.
        |                      compileFromString$package$foo$1
        |                  (completion.asInstanceOf[continuations.Continuation[Any | Null]])
        |            }
        |          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
        |            $continuation.asInstanceOf[
        |              continuations.compileFromString$package.
        |                compileFromString$package$foo$1
        |            ].$result
        |          $continuation.asInstanceOf[
        |            continuations.compileFromString$package.
        |              compileFromString$package$foo$1
        |          ].$label match
        |            {
        |              case 0 =>
        |                if $result.!=(null) then
        |                  $result.fold[Unit](
        |                    {
        |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
        |                      closure($anonfun)
        |                    }
        |                  ,
        |                    {
        |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
        |                      closure($anonfun)
        |                    }
        |                  )
        |                 else ()
        |                xx = 111
        |                println(xx)
        |                $continuation.asInstanceOf[
        |                  continuations.compileFromString$package.
        |                    compileFromString$package$foo$1
        |                ].I$0 = xx
        |                $continuation.asInstanceOf[
        |                  continuations.compileFromString$package.
        |                    compileFromString$package$foo$1
        |                ].$label = 1
        |                val safeContinuation: continuations.SafeContinuation[Int] =
        |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
        |                    continuations.Continuation.State.Undecided
        |                  )
        |                safeContinuation.resume(Right.apply[Nothing, Int](10))
        |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
        |                  safeContinuation.getOrThrow()
        |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
        |                ()
        |              case 1 =>
        |                xx =
        |                  $continuation.asInstanceOf[
        |                    continuations.compileFromString$package.
        |                      compileFromString$package$foo$1
        |                  ].I$0
        |                if $result.!=(null) then
        |                  $result.fold[Unit](
        |                    {
        |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
        |                      closure($anonfun)
        |                    }
        |                  ,
        |                    {
        |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
        |                      closure($anonfun)
        |                    }
        |                  )
        |                 else ()
        |                ()
        |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
        |            }
        |        }
        |        xx
        |      }
        |  }
        |}
        |""".stripMargin
      
  val expectedStateMachineManyDependantContinuations =
    """
      package continuations {
      |  final lazy module val compileFromString$package:
      |    continuations.compileFromString$package
      |   = new continuations.compileFromString$package()
      |  @SourceFile("compileFromString.scala") final module class
      |    compileFromString$package
      |  () extends Object() { this: continuations.compileFromString$package.type =>
      |    private def writeReplace(): AnyRef =
      |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
      |    class compileFromString$package$fooTest$1($completion: continuations.Continuation[Any | Null]) extends
      |      continuations.jvm.internal.ContinuationImpl
      |    ($completion, $completion.context) {
      |      var I$0: Any = _
      |      var I$1: Any = _
      |      var I$2: Any = _
      |      var I$3: Any = _
      |      var I$4: Any = _
      |      var I$5: Any = _
      |      var I$6: Any = _
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
      |      def I$5_=(x$0: Any): Unit = ()
      |      def I$6_=(x$0: Any): Unit = ()
      |      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      |      var $label: Int = _
      |      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
      |      def $label_=(x$0: Int): Unit = ()
      |      protected override def invokeSuspend(
      |        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
      |      ): Any | Null =
      |        {
      |          this.$result = result
      |          this.$label = this.$label.|(scala.Int.MinValue)
      |          continuations.compileFromString$package.fooTest(null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(qq: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var qq##1: Int = qq
      |        var pp: Int = null
      |        var xx: Int = null
      |        var ww: Int = null
      |        var yy: String = null
      |        var tt: Int = null
      |        var zz: Int = null
      |        {
      |          var $continuation: continuations.Continuation[Any] | Null = null
      |          completion match
      |            {
      |              case x$0 @ <empty> if
      |                x$0.isInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].&&(
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].$label.&(scala.Int.MinValue).!=(0)
      |                )
      |               =>
      |                $continuation =
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ]
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].$label.-(scala.Int.MinValue)
      |              case _ =>
      |                $continuation =
      |                  new
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  (completion.asInstanceOf[continuations.Continuation[Any | Null]])
      |            }
      |          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
      |            $continuation.asInstanceOf[
      |              continuations.compileFromString$package.
      |                compileFromString$package$fooTest$1
      |            ].$result
      |          $continuation.asInstanceOf[
      |            continuations.compileFromString$package.
      |              compileFromString$package$fooTest$1
      |          ].$label match
      |            {
      |              case 0 =>
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                pp = 11
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = pp
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, Int](
      |                    {
      |                      qq##1.-(1)
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                xx = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                pp =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                xx = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                ww = 13
      |                val rr: String = "AAA"
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = pp
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = xx
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = ww
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[String] =
      |                  new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, String](
      |                    {
      |                      rr
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                yy = orThrow.asInstanceOf[String]
      |                return[label2] ()
      |                ()
      |              case 2 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                pp =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                xx =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                ww =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                yy = $result.asInstanceOf[String]
      |                label2[Unit]: <empty>
      |                tt = 100
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = pp
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = xx
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = ww
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = yy
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$5 = tt
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 3
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, Int](
      |                    {
      |                      ww.-(1)
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                zz = orThrow.asInstanceOf[Int]
      |                ()
      |              case 3 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                pp =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                xx =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                ww =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                yy =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                tt =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$5
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                zz = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        println(xx)
      |        xx.+(qq##1).+(augmentString(yy).size).+(zz).+(pp).+(tt)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineManyDependantAndNoDependantContinuations =
    """
      |package continuations {
      |  final lazy module val compileFromString$package:
      |    continuations.compileFromString$package
      |   = new continuations.compileFromString$package()
      |  @SourceFile("compileFromString.scala") final module class
      |    compileFromString$package
      |  () extends Object() { this: continuations.compileFromString$package.type =>
      |    private def writeReplace(): AnyRef =
      |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
      |    class compileFromString$package$fooTest$1($completion: continuations.Continuation[Any | Null]) extends
      |      continuations.jvm.internal.ContinuationImpl
      |    ($completion, $completion.context) {
      |      var I$0: Any = _
      |      var I$1: Any = _
      |      var I$2: Any = _
      |      var I$3: Any = _
      |      var I$4: Any = _
      |      var I$5: Any = _
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
      |      def I$5_=(x$0: Any): Unit = ()
      |      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      |      var $label: Int = _
      |      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
      |      def $label_=(x$0: Int): Unit = ()
      |      protected override def invokeSuspend(
      |        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
      |      ): Any | Null =
      |        {
      |          this.$result = result
      |          this.$label = this.$label.|(scala.Int.MinValue)
      |          continuations.compileFromString$package.fooTest(null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(qq: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var qq##1: Int = qq
      |        var pp: Int = null
      |        var xx: Int = null
      |        var ww: Int = null
      |        var tt: Int = null
      |        var zz: Int = null
      |        {
      |          var $continuation: continuations.Continuation[Any] | Null = null
      |          completion match
      |            {
      |              case x$0 @ <empty> if
      |                x$0.isInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].&&(
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].$label.&(scala.Int.MinValue).!=(0)
      |                )
      |               =>
      |                $continuation =
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ]
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].$label.-(scala.Int.MinValue)
      |              case _ =>
      |                $continuation =
      |                  new
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  (completion.asInstanceOf[continuations.Continuation[Any | Null]])
      |            }
      |          val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
      |            $continuation.asInstanceOf[
      |              continuations.compileFromString$package.
      |                compileFromString$package$fooTest$1
      |            ].$result
      |          $continuation.asInstanceOf[
      |            continuations.compileFromString$package.
      |              compileFromString$package$fooTest$1
      |          ].$label match
      |            {
      |              case 0 =>
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                pp = 11
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = pp
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, Int](
      |                    {
      |                      qq##1.-(1)
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                xx = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                pp =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                xx = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                ww = 13
      |                val rr: String = "AAA"
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = pp
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = xx
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = ww
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[String] =
      |                  new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, String](
      |                    {
      |                      rr
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                return[label2] ()
      |                ()
      |              case 2 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                pp =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                xx =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                ww =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                label2[Unit]: <empty>
      |                tt = 100
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = qq##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = pp
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = xx
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = ww
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = tt
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 3
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, Int](
      |                    {
      |                      ww.-(1)
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                zz = orThrow.asInstanceOf[Int]
      |                ()
      |              case 3 =>
      |                qq##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                pp =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                xx =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                ww =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                tt =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                if $result.!=(null) then
      |                  $result.fold[Unit](
      |                    {
      |                      def $anonfun(val x$0: Throwable): Nothing = throw x$0
      |                      closure($anonfun)
      |                    }
      |                  ,
      |                    {
      |                      def $anonfun(val x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
      |                      closure($anonfun)
      |                    }
      |                  )
      |                 else ()
      |                zz = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        println(xx)
      |        xx.+(qq##1).+(zz).+(pp).+(tt)
      |      }
      |  }
      |}
      |""".stripMargin
}
