package continuations

trait StateMachineFixtures {

  val expectedOneSuspendContinuation =
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
       |    def foo(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
       |      {
       |        val continuation1: continuations.Continuation[Int] = completion
       |        val safeContinuation: continuations.SafeContinuation[Int] = 
       |          new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
       |            continuations.Continuation.State.Undecided
       |          )
       |        safeContinuation.resume(Right.apply[Nothing, Int](1))
       |        safeContinuation.getOrThrow()
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineForSuspendContinuationReturningANonSuspendingVal =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            {
       |              var $continuation: continuations.Continuation[Any] | Null = null
       |              completion match 
       |                {
       |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                    $continuation = x$0
       |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |                }
       |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |                $continuation.asInstanceOf[program$foo$1].$result
       |              $continuation.asInstanceOf[program$foo$1].$label match 
       |                {
       |                  case 0 => 
       |                    continuations.Continuation.checkResult($result)
       |                    $continuation.asInstanceOf[program$foo$1].$label = 1
       |                    val safeContinuation: continuations.SafeContinuation[Int] = 
       |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                        continuations.Continuation.State.Undecided
       |                      )
       |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    ()
       |                  case 1 => 
       |                    continuations.Continuation.checkResult($result)
       |                    ()
       |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |                }
       |            }
       |            10
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineWithSingleSuspendedContinuationReturningANonSuspendedVal =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var I$0: Any = _
       |          def I$0_=(x$0: Any): Unit = ()
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(null, this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(x: Int, completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            var x##1: Int = x
       |            {
       |              var $continuation: continuations.Continuation[Any] | Null = null
       |              completion match 
       |                {
       |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                    $continuation = x$0
       |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |                }
       |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |                $continuation.asInstanceOf[program$foo$1].$result
       |              $continuation.asInstanceOf[program$foo$1].$label match 
       |                {
       |                  case 0 => 
       |                    continuations.Continuation.checkResult($result)
       |                    $continuation.asInstanceOf[program$foo$1].I$0 = x##1
       |                    $continuation.asInstanceOf[program$foo$1].$label = 1
       |                    val safeContinuation: continuations.SafeContinuation[Int] = 
       |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                        continuations.Continuation.State.Undecided
       |                      )
       |                    safeContinuation.resume(Right.apply[Nothing, Int](x##1))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    ()
       |                  case 1 => 
       |                    x##1 = $continuation.asInstanceOf[program$foo$1].I$0
       |                    continuations.Continuation.checkResult($result)
       |                    ()
       |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |                }
       |            }
       |            10
       |          }
       |        foo(11, continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineMultipleSuspendedContinuationsReturningANonSuspendingVal =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            {
       |              var $continuation: continuations.Continuation[Any] | Null = null
       |              completion match 
       |                {
       |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                    $continuation = x$0
       |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |                }
       |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |                $continuation.asInstanceOf[program$foo$1].$result
       |              $continuation.asInstanceOf[program$foo$1].$label match 
       |                {
       |                  case 0 => 
       |                    continuations.Continuation.checkResult($result)
       |                    $continuation.asInstanceOf[program$foo$1].$label = 1
       |                    val safeContinuation: continuations.SafeContinuation[Int] = 
       |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                        continuations.Continuation.State.Undecided
       |                      )
       |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    return[label1] ()
       |                    ()
       |                  case 1 => 
       |                    continuations.Continuation.checkResult($result)
       |                    label1[Unit]: <empty>
       |                    $continuation.asInstanceOf[program$foo$1].$label = 2
       |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
       |                      new continuations.SafeContinuation[Boolean](
       |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
       |                      , continuations.Continuation.State.Undecided)
       |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    return[label2] ()
       |                    ()
       |                  case 2 => 
       |                    continuations.Continuation.checkResult($result)
       |                    label2[Unit]: <empty>
       |                    $continuation.asInstanceOf[program$foo$1].$label = 3
       |                    val safeContinuation: continuations.SafeContinuation[String] = 
       |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
       |                        , 
       |                      continuations.Continuation.State.Undecided)
       |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    ()
       |                  case 3 => 
       |                    continuations.Continuation.checkResult($result)
       |                    ()
       |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |                }
       |            }
       |            10
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineWithMultipleResumeReturningANonSuspendedValue =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            {
       |              var $continuation: continuations.Continuation[Any] | Null = null
       |              completion match 
       |                {
       |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                    $continuation = x$0
       |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |                }
       |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |                $continuation.asInstanceOf[program$foo$1].$result
       |              $continuation.asInstanceOf[program$foo$1].$label match 
       |                {
       |                  case 0 => 
       |                    continuations.Continuation.checkResult($result)
       |                    $continuation.asInstanceOf[program$foo$1].$label = 1
       |                    val safeContinuation: continuations.SafeContinuation[Int] = 
       |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                        continuations.Continuation.State.Undecided
       |                      )
       |                    println("Hello")
       |                    println("World")
       |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    return[label1] ()
       |                    ()
       |                  case 1 => 
       |                    continuations.Continuation.checkResult($result)
       |                    label1[Unit]: <empty>
       |                    $continuation.asInstanceOf[program$foo$1].$label = 2
       |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
       |                      new continuations.SafeContinuation[Boolean](
       |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
       |                      , continuations.Continuation.State.Undecided)
       |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
       |                    safeContinuation.resume(Right.apply[Nothing, Boolean](true))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    return[label2] ()
       |                    ()
       |                  case 2 => 
       |                    continuations.Continuation.checkResult($result)
       |                    label2[Unit]: <empty>
       |                    $continuation.asInstanceOf[program$foo$1].$label = 3
       |                    val safeContinuation: continuations.SafeContinuation[String] = 
       |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
       |                        , 
       |                      continuations.Continuation.State.Undecided)
       |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
       |                    val x: Int = 1
       |                    ()
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    ()
       |                  case 3 => 
       |                    continuations.Continuation.checkResult($result)
       |                    ()
       |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |                }
       |            }
       |            10
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineReturningANonSuspendedValue =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            {
       |              var $continuation: continuations.Continuation[Any] | Null = null
       |              completion match 
       |                {
       |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                    $continuation = x$0
       |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |                }
       |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |                $continuation.asInstanceOf[program$foo$1].$result
       |              $continuation.asInstanceOf[program$foo$1].$label match 
       |                {
       |                  case 0 => 
       |                    continuations.Continuation.checkResult($result)
       |                    println("Start")
       |                    $continuation.asInstanceOf[program$foo$1].$label = 1
       |                    val safeContinuation: continuations.SafeContinuation[Int] = 
       |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                        continuations.Continuation.State.Undecided
       |                      )
       |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    return[label1] ()
       |                    ()
       |                  case 1 => 
       |                    continuations.Continuation.checkResult($result)
       |                    label1[Unit]: <empty>
       |                    val x: String = "World"
       |                    println("Hello")
       |                    println(x)
       |                    $continuation.asInstanceOf[program$foo$1].$label = 2
       |                    val safeContinuation: continuations.SafeContinuation[Int] = 
       |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                        continuations.Continuation.State.Undecided
       |                      )
       |                    safeContinuation.resume(Right.apply[Nothing, Int](2))
       |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                      safeContinuation.getOrThrow()
       |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                    ()
       |                  case 2 => 
       |                    continuations.Continuation.checkResult($result)
       |                    ()
       |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |                }
       |            }
       |            println("End")
       |            10
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineNoDependantSuspensions =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            var $continuation: continuations.Continuation[Any] | Null = null
       |            completion match 
       |              {
       |                case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                  $continuation = x$0
       |                  $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |              }
       |            val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |              $continuation.asInstanceOf[program$foo$1].$result
       |            $continuation.asInstanceOf[program$foo$1].$label match 
       |              {
       |                case 0 => 
       |                  continuations.Continuation.checkResult($result)
       |                  $continuation.asInstanceOf[program$foo$1].$label = 1
       |                  val safeContinuation: continuations.SafeContinuation[Boolean] = 
       |                    new continuations.SafeContinuation[Boolean](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
       |                      , 
       |                    continuations.Continuation.State.Undecided)
       |                  safeContinuation.resume(Right.apply[Nothing, Boolean](false))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  return[label1] ()
       |                  ()
       |                case 1 => 
       |                  continuations.Continuation.checkResult($result)
       |                  label1[Unit]: <empty>
       |                  $continuation.asInstanceOf[program$foo$1].$label = 2
       |                  val safeContinuation: continuations.SafeContinuation[String] = 
       |                    new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)(), 
       |                      continuations.Continuation.State.Undecided
       |                    )
       |                  safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  return[label2] ()
       |                  ()
       |                case 2 => 
       |                  continuations.Continuation.checkResult($result)
       |                  label2[Unit]: <empty>
       |                  $continuation.asInstanceOf[program$foo$1].$label = 3
       |                  val safeContinuation: continuations.SafeContinuation[Int] = 
       |                    new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                      continuations.Continuation.State.Undecided
       |                    )
       |                  safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  orThrow
       |                case 3 => 
       |                  continuations.Continuation.checkResult($result)
       |                  $result
       |                case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |              }
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineNoDependantSuspensionsWithCodeInside =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            var $continuation: continuations.Continuation[Any] | Null = null
       |            completion match 
       |              {
       |                case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                  $continuation = x$0
       |                  $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |              }
       |            val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |              $continuation.asInstanceOf[program$foo$1].$result
       |            $continuation.asInstanceOf[program$foo$1].$label match 
       |              {
       |                case 0 => 
       |                  continuations.Continuation.checkResult($result)
       |                  $continuation.asInstanceOf[program$foo$1].$label = 1
       |                  val safeContinuation: continuations.SafeContinuation[Boolean] = 
       |                    new continuations.SafeContinuation[Boolean](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
       |                      , 
       |                    continuations.Continuation.State.Undecided)
       |                  println("Hi")
       |                  safeContinuation.resume(Right.apply[Nothing, Boolean](false))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  return[label1] ()
       |                  ()
       |                case 1 => 
       |                  continuations.Continuation.checkResult($result)
       |                  label1[Unit]: <empty>
       |                  $continuation.asInstanceOf[program$foo$1].$label = 2
       |                  val safeContinuation: continuations.SafeContinuation[String] = 
       |                    new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)(), 
       |                      continuations.Continuation.State.Undecided
       |                    )
       |                  safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
       |                  println("World")
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  return[label2] ()
       |                  ()
       |                case 2 => 
       |                  continuations.Continuation.checkResult($result)
       |                  label2[Unit]: <empty>
       |                  $continuation.asInstanceOf[program$foo$1].$label = 3
       |                  val safeContinuation: continuations.SafeContinuation[Int] = 
       |                    new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                      continuations.Continuation.State.Undecided
       |                    )
       |                  safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  orThrow
       |                case 3 => 
       |                  continuations.Continuation.checkResult($result)
       |                  $result
       |                case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |              }
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

  val expectedStateMachineNoDependantSuspensionsWithCodeBetween =
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
       |    def program: Int = 
       |      {
       |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
       |          $completion.context
       |        ) {
       |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
       |          var $label: Int = _
       |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
       |             = 
       |          ()
       |          def $label_=(x$0: Int): Unit = ()
       |          protected override def invokeSuspend(
       |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
       |          ): Any | Null = 
       |            {
       |              this.$result = result
       |              this.$label = this.$label.|(scala.Int.MinValue)
       |              foo(this.asInstanceOf[continuations.Continuation[Int]])
       |            }
       |        }
       |        def foo(completion: continuations.Continuation[Int]): 
       |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
       |         = 
       |          {
       |            var $continuation: continuations.Continuation[Any] | Null = null
       |            completion match 
       |              {
       |                case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
       |                  $continuation = x$0
       |                  $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
       |                case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
       |              }
       |            val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
       |              $continuation.asInstanceOf[program$foo$1].$result
       |            $continuation.asInstanceOf[program$foo$1].$label match 
       |              {
       |                case 0 => 
       |                  continuations.Continuation.checkResult($result)
       |                  println("Start")
       |                  val x: Int = 1
       |                  $continuation.asInstanceOf[program$foo$1].$label = 1
       |                  val safeContinuation: continuations.SafeContinuation[Boolean] = 
       |                    new continuations.SafeContinuation[Boolean](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
       |                      , 
       |                    continuations.Continuation.State.Undecided)
       |                  safeContinuation.resume(Right.apply[Nothing, Boolean](false))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  return[label1] ()
       |                  ()
       |                case 1 => 
       |                  continuations.Continuation.checkResult($result)
       |                  label1[Unit]: <empty>
       |                  println("Hello")
       |                  $continuation.asInstanceOf[program$foo$1].$label = 2
       |                  val safeContinuation: continuations.SafeContinuation[String] = 
       |                    new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)(), 
       |                      continuations.Continuation.State.Undecided
       |                    )
       |                  safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  return[label2] ()
       |                  ()
       |                case 2 => 
       |                  continuations.Continuation.checkResult($result)
       |                  label2[Unit]: <empty>
       |                  $continuation.asInstanceOf[program$foo$1].$label = 3
       |                  val safeContinuation: continuations.SafeContinuation[Int] = 
       |                    new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
       |                      continuations.Continuation.State.Undecided
       |                    )
       |                  safeContinuation.resume(Right.apply[Nothing, Int](1))
       |                  val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
       |                    safeContinuation.getOrThrow()
       |                  if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
       |                  orThrow
       |                case 3 => 
       |                  continuations.Continuation.checkResult($result)
       |                  $result
       |                case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |              }
       |          }
       |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
       |      }
       |  }
       |}
       |""".stripMargin

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
       |                $continuation = x$0
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
       |                continuations.Continuation.checkResult($result)
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
       |                continuations.Continuation.checkResult($result)
       |                y = $result.asInstanceOf[Int]
       |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
       |            }
       |        }
       |        x##1.+(y)
       |      }
       |  }
       |}
         """.stripMargin

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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, Unit](println(qq##1)))
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
      |                continuations.Continuation.checkResult($result)
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
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
      |                continuations.Continuation.checkResult($result)
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, Int](qq##1.-(1)))
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, String](rr))
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, Int](ww.-(1)))
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
      |                continuations.Continuation.checkResult($result)
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, Int](qq##1.-(1)))
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, String](rr))
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
      |                continuations.Continuation.checkResult($result)
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
      |                safeContinuation.resume(Right.apply[Nothing, Int](ww.-(1)))
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
      |                continuations.Continuation.checkResult($result)
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

  val expectedStateMachineWithDependantAndNoDependantContinuationAtTheEnd =
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
      |          continuations.compileFromString$package.fooTest(null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, completion: continuations.Continuation[Int]):
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
      |                    compileFromString$package$fooTest$1
      |                ].&&(
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].$label.&(scala.Int.MinValue).!=(0)
      |                )
      |               =>
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                println("Hello")
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x##1
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
      |                      println("World")
      |                      1
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                y = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                continuations.Continuation.checkResult($result)
      |                y = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val z: Int = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = y
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                val w: String = "World"
      |                println("Hello")
      |                safeContinuation.resume(
      |                  Right.apply[Nothing, Int](
      |                    {
      |                      println(z)
      |                      x##1
      |                    }
      |                  )
      |                )
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                ()
      |              case 2 =>
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                y =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                ()
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        val tt: Int = 2
      |        x##1.+(y).+(tt)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineForOneChainedContinuation =
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
      |          continuations.compileFromString$package.fooTest(this.asInstanceOf[continuations.Continuation[Int]])
      |        }
      |    }
      |    def fooTest(completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](1))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                x = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                continuations.Continuation.checkResult($result)
      |                x = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                x =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineMultipleChainedSuspendContinuationsReturningANonSuspendedVal =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
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
      |    def fooTest(q: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var q##1: Int = q
      |        var x: Int = null
      |        var j: Int = null
      |        var w: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                println("Hello")
      |                val z: Int = 100
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = q##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](1.+(z)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                x = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                q##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                continuations.Continuation.checkResult($result)
      |                x = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                j = 9
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = q##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = j
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x.+(1).+(q##1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                w = orThrow.asInstanceOf[Int]
      |                return[label2] ()
      |                ()
      |              case 2 =>
      |                q##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                j =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                w = $result.asInstanceOf[Int]
      |                label2[Unit]: <empty>
      |                println("World")
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = q##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = j
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = w
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 3
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x.+(w).+(1).+(j)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                ()
      |              case 3 =>
      |                q##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                j =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                w =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                ()
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        10
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineChainedSuspendContinuationsOneParameter =
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
      |          continuations.compileFromString$package.fooTest(null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, completion: continuations.Continuation[Int]):
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
      |                    compileFromString$package$fooTest$1
      |                ].&&(
      |                  x$0.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].$label.&(scala.Int.MinValue).!=(0)
      |                )
      |               =>
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                y = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                continuations.Continuation.checkResult($result)
      |                y = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = y
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](y.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                y =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineChainedSuspendContinuationsOneParameterAndVals =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
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
      |    def fooTest(x: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var q: Int = null
      |        var y: Int = null
      |        var p: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                q = 2
      |                val w: Int = 3
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = q
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(w)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                y = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                q =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                y = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                p = 1
      |                val t: Int = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = q
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = y
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = p
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](y.+(q).+(x##1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                ()
      |              case 2 =>
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                q =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                y =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                p =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        z.+(y).+(p)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChained =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedOneGenericParam =
    """
      |package continuations {
      |  @SourceFile("compileFromString.scala") case class Foo(x: Int) extends Object(), _root_.scala.Product, _root_.
      |    scala
      |  .Serializable {
      |    override def hashCode(): Int =
      |      {
      |        var acc: Int = -889275714
      |        acc = scala.runtime.Statics#mix(acc, this.productPrefix.hashCode())
      |        acc = scala.runtime.Statics#mix(acc, Foo.this.x)
      |        scala.runtime.Statics#finalizeHash(acc, 1)
      |      }
      |    override def equals(x$0: Any): Boolean =
      |      this.eq(x$0.$asInstanceOf[Object]).||(
      |        x$0 match
      |          {
      |            case x$0 @ _:continuations.Foo @unchecked => this.x.==(x$0.x).&&(x$0.canEqual(this))
      |            case _ => false
      |          }
      |      )
      |    override def toString(): String = scala.runtime.ScalaRunTime._toString(this)
      |    override def canEqual(that: Any): Boolean = that.isInstanceOf[continuations.Foo @unchecked]
      |    override def productArity: Int = 1
      |    override def productPrefix: String = "Foo"
      |    override def productElement(n: Int): Any =
      |      n match
      |        {
      |          case 0 => this._1
      |          case _ => throw new IndexOutOfBoundsException(n.toString())
      |        }
      |    override def productElementName(n: Int): String =
      |      n match
      |        {
      |          case 0 => "x"
      |          case _ => throw new IndexOutOfBoundsException(n.toString())
      |        }
      |    val x: Int
      |    def copy(x: Int): continuations.Foo = new continuations.Foo(x)
      |    def copy$default$1: Int @uncheckedVariance = Foo.this.x
      |    def _1: Int = this.x
      |  }
      |  final lazy module val Foo: continuations.Foo = new continuations.Foo()
      |  @SourceFile("compileFromString.scala") final module class Foo() extends AnyRef(), scala.deriving.Mirror.
      |    Product
      |   { this: continuations.Foo.type =>
      |    private def writeReplace(): AnyRef = new scala.runtime.ModuleSerializationProxy(classOf[continuations.Foo.type])
      |    def apply(x: Int): continuations.Foo = new continuations.Foo(x)
      |    def unapply(x$1: continuations.Foo): continuations.Foo = x$1
      |    override def toString: String = "Foo"
      |    type MirroredMonoType = continuations.Foo
      |    def fromProduct(x$0: Product): continuations.Foo.MirroredMonoType = new continuations.Foo(x$0.productElement(0).$asInstanceOf[Int])
      |  }
      |  final lazy module val compileFromString$package:
      |    continuations.compileFromString$package
      |   = new continuations.compileFromString$package()
      |  @SourceFile("compileFromString.scala") final module class
      |    compileFromString$package
      |  () extends Object() { this: continuations.compileFromString$package.type =>
      |    private def writeReplace(): AnyRef =
      |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
      |    def program: continuations.Foo =
      |      {
      |        class program$fooTest$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion
      |          , 
      |        $completion.context) {
      |          var I$0: Any = _
      |          var I$1: Any = _
      |          var I$2: Any = _
      |          def I$0_=(x$0: Any): Unit = ()
      |          def I$1_=(x$0: Any): Unit = ()
      |          def I$2_=(x$0: Any): Unit = ()
      |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      |          var $label: Int = _
      |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
      |             =
      |          ()
      |          def $label_=(x$0: Int): Unit = ()
      |          protected override def invokeSuspend(
      |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
      |          ): Any | Null =
      |            {
      |              this.$result = result
      |              this.$label = this.$label.|(scala.Int.MinValue)
      |              fooTest(null, null, this.asInstanceOf[continuations.Continuation[A]])
      |            }
      |        }
      |        def fooTest(a: A, b: Int, completion: continuations.Continuation[A]):
      |          A | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |         =
      |          {
      |            var a##1: A = a
      |            var b##1: Int = b
      |            var z: A = null
      |            {
      |              var $continuation: continuations.Continuation[Any] | Null = null
      |              completion match
      |                {
      |                  case x$0 @ <empty> if
      |                    x$0.isInstanceOf[program$fooTest$1].&&(x$0.asInstanceOf[program$fooTest$1].$label.&(scala.Int.MinValue).!=(0))
      |                   =>
      |                    $continuation = x$0
      |                    $continuation.asInstanceOf[program$fooTest$1].$label = $continuation.asInstanceOf[program$fooTest$1].$label.-(scala.Int.MinValue)
      |                  case _ => $continuation = new program$fooTest$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
      |                }
      |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
      |                $continuation.asInstanceOf[program$fooTest$1].$result
      |              $continuation.asInstanceOf[program$fooTest$1].$label match
      |                {
      |                  case 0 =>
      |                    continuations.Continuation.checkResult($result)
      |                    $continuation.asInstanceOf[program$fooTest$1].I$0 = b##1
      |                    $continuation.asInstanceOf[program$fooTest$1].I$1 = a##1
      |                    $continuation.asInstanceOf[program$fooTest$1].$label = 1
      |                    val safeContinuation: continuations.SafeContinuation[A] =
      |                      new continuations.SafeContinuation[A](continuations.intrinsics.IntrinsicsJvm$package.intercepted[A]($continuation)(),
      |                        continuations.Continuation.State.Undecided
      |                      )
      |                    safeContinuation.resume(Right.apply[Nothing, A](a##1))
      |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                      safeContinuation.getOrThrow()
      |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                    z = orThrow.asInstanceOf[A]
      |                    return[label1] ()
      |                    ()
      |                  case 1 =>
      |                    b##1 = $continuation.asInstanceOf[program$fooTest$1].I$0
      |                    a##1 = $continuation.asInstanceOf[program$fooTest$1].I$1
      |                    continuations.Continuation.checkResult($result)
      |                    z = $result.asInstanceOf[A]
      |                    label1[Unit]: <empty>
      |                    $continuation.asInstanceOf[program$fooTest$1].I$0 = b##1
      |                    $continuation.asInstanceOf[program$fooTest$1].I$1 = a##1
      |                    $continuation.asInstanceOf[program$fooTest$1].I$2 = z
      |                    $continuation.asInstanceOf[program$fooTest$1].$label = 2
      |                    val safeContinuation: continuations.SafeContinuation[A] =
      |                      new continuations.SafeContinuation[A](continuations.intrinsics.IntrinsicsJvm$package.intercepted[A]($continuation)(),
      |                        continuations.Continuation.State.Undecided
      |                      )
      |                    safeContinuation.resume(
      |                      Right.apply[Nothing, A](
      |                        {
      |                          println("World")
      |                          z
      |                        }
      |                      )
      |                    )
      |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                      safeContinuation.getOrThrow()
      |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                    orThrow
      |                  case 2 =>
      |                    b##1 = $continuation.asInstanceOf[program$fooTest$1].I$0
      |                    a##1 = $continuation.asInstanceOf[program$fooTest$1].I$1
      |                    z = $continuation.asInstanceOf[program$fooTest$1].I$2
      |                    continuations.Continuation.checkResult($result)
      |                    $result
      |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |                }
      |            }
      |          }
      |        fooTest(continuations.Foo.apply(1), 1, continuations.jvm.internal.ContinuationStub.contImpl)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoGenericParams =
    """
      |package continuations {
      |  @SourceFile("compileFromString.scala") case class Foo(x: Int) extends Object(), _root_.scala.Product, _root_.
      |    scala
      |  .Serializable {
      |    override def hashCode(): Int =
      |      {
      |        var acc: Int = -889275714
      |        acc = scala.runtime.Statics#mix(acc, this.productPrefix.hashCode())
      |        acc = scala.runtime.Statics#mix(acc, Foo.this.x)
      |        scala.runtime.Statics#finalizeHash(acc, 1)
      |      }
      |    override def equals(x$0: Any): Boolean =
      |      this.eq(x$0.$asInstanceOf[Object]).||(
      |        x$0 match
      |          {
      |            case x$0 @ _:continuations.Foo @unchecked => this.x.==(x$0.x).&&(x$0.canEqual(this))
      |            case _ => false
      |          }
      |      )
      |    override def toString(): String = scala.runtime.ScalaRunTime._toString(this)
      |    override def canEqual(that: Any): Boolean = that.isInstanceOf[continuations.Foo @unchecked]
      |    override def productArity: Int = 1
      |    override def productPrefix: String = "Foo"
      |    override def productElement(n: Int): Any =
      |      n match
      |        {
      |          case 0 => this._1
      |          case _ => throw new IndexOutOfBoundsException(n.toString())
      |        }
      |    override def productElementName(n: Int): String =
      |      n match
      |        {
      |          case 0 => "x"
      |          case _ => throw new IndexOutOfBoundsException(n.toString())
      |        }
      |    val x: Int
      |    def copy(x: Int): continuations.Foo = new continuations.Foo(x)
      |    def copy$default$1: Int @uncheckedVariance = Foo.this.x
      |    def _1: Int = this.x
      |  }
      |  final lazy module val Foo: continuations.Foo = new continuations.Foo()
      |  @SourceFile("compileFromString.scala") final module class Foo() extends AnyRef(), scala.deriving.Mirror.
      |    Product
      |   { this: continuations.Foo.type =>
      |    private def writeReplace(): AnyRef = new scala.runtime.ModuleSerializationProxy(classOf[continuations.Foo.type])
      |    def apply(x: Int): continuations.Foo = new continuations.Foo(x)
      |    def unapply(x$1: continuations.Foo): continuations.Foo = x$1
      |    override def toString: String = "Foo"
      |    type MirroredMonoType = continuations.Foo
      |    def fromProduct(x$0: Product): continuations.Foo.MirroredMonoType = new continuations.Foo(x$0.productElement(0).$asInstanceOf[Int])
      |  }
      |  @SourceFile("compileFromString.scala") case class Bar(x: Int) extends Object(), _root_.scala.Product, _root_.
      |    scala
      |  .Serializable {
      |    override def hashCode(): Int =
      |      {
      |        var acc: Int = -889275714
      |        acc = scala.runtime.Statics#mix(acc, this.productPrefix.hashCode())
      |        acc = scala.runtime.Statics#mix(acc, Bar.this.x)
      |        scala.runtime.Statics#finalizeHash(acc, 1)
      |      }
      |    override def equals(x$0: Any): Boolean =
      |      this.eq(x$0.$asInstanceOf[Object]).||(
      |        x$0 match
      |          {
      |            case x$0 @ _:continuations.Bar @unchecked => this.x.==(x$0.x).&&(x$0.canEqual(this))
      |            case _ => false
      |          }
      |      )
      |    override def toString(): String = scala.runtime.ScalaRunTime._toString(this)
      |    override def canEqual(that: Any): Boolean = that.isInstanceOf[continuations.Bar @unchecked]
      |    override def productArity: Int = 1
      |    override def productPrefix: String = "Bar"
      |    override def productElement(n: Int): Any =
      |      n match
      |        {
      |          case 0 => this._1
      |          case _ => throw new IndexOutOfBoundsException(n.toString())
      |        }
      |    override def productElementName(n: Int): String =
      |      n match
      |        {
      |          case 0 => "x"
      |          case _ => throw new IndexOutOfBoundsException(n.toString())
      |        }
      |    val x: Int
      |    def copy(x: Int): continuations.Bar = new continuations.Bar(x)
      |    def copy$default$1: Int @uncheckedVariance = Bar.this.x
      |    def _1: Int = this.x
      |  }
      |  final lazy module val Bar: continuations.Bar = new continuations.Bar()
      |  @SourceFile("compileFromString.scala") final module class Bar() extends AnyRef(), scala.deriving.Mirror.
      |    Product
      |   { this: continuations.Bar.type =>
      |    private def writeReplace(): AnyRef = new scala.runtime.ModuleSerializationProxy(classOf[continuations.Bar.type])
      |    def apply(x: Int): continuations.Bar = new continuations.Bar(x)
      |    def unapply(x$1: continuations.Bar): continuations.Bar = x$1
      |    override def toString: String = "Bar"
      |    type MirroredMonoType = continuations.Bar
      |    def fromProduct(x$0: Product): continuations.Bar.MirroredMonoType = new continuations.Bar(x$0.productElement(0).$asInstanceOf[Int])
      |  }
      |  final lazy module val compileFromString$package:
      |    continuations.compileFromString$package
      |   = new continuations.compileFromString$package()
      |  @SourceFile("compileFromString.scala") final module class
      |    compileFromString$package
      |  () extends Object() { this: continuations.compileFromString$package.type =>
      |    private def writeReplace(): AnyRef =
      |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
      |    def program: continuations.Bar =
      |      {
      |        class program$fooTest$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion
      |          , 
      |        $completion.context) {
      |          var I$0: Any = _
      |          var I$1: Any = _
      |          var I$2: Any = _
      |          def I$0_=(x$0: Any): Unit = ()
      |          def I$1_=(x$0: Any): Unit = ()
      |          def I$2_=(x$0: Any): Unit = ()
      |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      |          var $label: Int = _
      |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
      |             =
      |          ()
      |          def $label_=(x$0: Int): Unit = ()
      |          protected override def invokeSuspend(
      |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
      |          ): Any | Null =
      |            {
      |              this.$result = result
      |              this.$label = this.$label.|(scala.Int.MinValue)
      |              fooTest(null, null, this.asInstanceOf[continuations.Continuation[B]])
      |            }
      |        }
      |        def fooTest(a: A, b: B, completion: continuations.Continuation[B]):
      |          B | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |         =
      |          {
      |            var a##1: A = a
      |            var b##1: B = b
      |            var z: A = null
      |            {
      |              var $continuation: continuations.Continuation[Any] | Null = null
      |              completion match
      |                {
      |                  case x$0 @ <empty> if
      |                    x$0.isInstanceOf[program$fooTest$1].&&(x$0.asInstanceOf[program$fooTest$1].$label.&(scala.Int.MinValue).!=(0))
      |                   =>
      |                    $continuation = x$0
      |                    $continuation.asInstanceOf[program$fooTest$1].$label = $continuation.asInstanceOf[program$fooTest$1].$label.-(scala.Int.MinValue)
      |                  case _ => $continuation = new program$fooTest$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
      |                }
      |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
      |                $continuation.asInstanceOf[program$fooTest$1].$result
      |              $continuation.asInstanceOf[program$fooTest$1].$label match
      |                {
      |                  case 0 =>
      |                    continuations.Continuation.checkResult($result)
      |                    $continuation.asInstanceOf[program$fooTest$1].I$0 = b##1
      |                    $continuation.asInstanceOf[program$fooTest$1].I$1 = a##1
      |                    $continuation.asInstanceOf[program$fooTest$1].$label = 1
      |                    val safeContinuation: continuations.SafeContinuation[A] =
      |                      new continuations.SafeContinuation[A](continuations.intrinsics.IntrinsicsJvm$package.intercepted[A]($continuation)(),
      |                        continuations.Continuation.State.Undecided
      |                      )
      |                    safeContinuation.resume(Right.apply[Nothing, A](a##1))
      |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                      safeContinuation.getOrThrow()
      |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                    z = orThrow.asInstanceOf[A]
      |                    return[label1] ()
      |                    ()
      |                  case 1 =>
      |                    b##1 = $continuation.asInstanceOf[program$fooTest$1].I$0
      |                    a##1 = $continuation.asInstanceOf[program$fooTest$1].I$1
      |                    continuations.Continuation.checkResult($result)
      |                    z = $result.asInstanceOf[A]
      |                    label1[Unit]: <empty>
      |                    $continuation.asInstanceOf[program$fooTest$1].I$0 = b##1
      |                    $continuation.asInstanceOf[program$fooTest$1].I$1 = a##1
      |                    $continuation.asInstanceOf[program$fooTest$1].I$2 = z
      |                    $continuation.asInstanceOf[program$fooTest$1].$label = 2
      |                    val safeContinuation: continuations.SafeContinuation[B] =
      |                      new continuations.SafeContinuation[B](continuations.intrinsics.IntrinsicsJvm$package.intercepted[B]($continuation)(),
      |                        continuations.Continuation.State.Undecided
      |                      )
      |                    safeContinuation.resume(
      |                      Right.apply[Nothing, B](
      |                        {
      |                          println(z)
      |                          b##1
      |                        }
      |                      )
      |                    )
      |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                      safeContinuation.getOrThrow()
      |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                    orThrow
      |                  case 2 =>
      |                    b##1 = $continuation.asInstanceOf[program$fooTest$1].I$0
      |                    a##1 = $continuation.asInstanceOf[program$fooTest$1].I$1
      |                    z = $continuation.asInstanceOf[program$fooTest$1].I$2
      |                    continuations.Continuation.checkResult($result)
      |                    $result
      |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |                }
      |            }
      |          }
      |        fooTest(continuations.Foo.apply(1), continuations.Bar.apply(2), continuations.jvm.internal.ContinuationStub.contImpl)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedExtraGivenParam =
    """
      |package continuations {
      |  import scala.concurrent.ExecutionContext
      |  import concurrent.ExecutionContext.Implicits.global
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, ec: concurrent.ExecutionContext, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var ec##1: concurrent.ExecutionContext = ec
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = ec##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                ec##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = ec##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                ec##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineContextFunctionTwoContinuationsChainedExtraGivenParam =
    """
      |package continuations {
      |  import scala.concurrent.ExecutionContext
      |  import concurrent.ExecutionContext.Implicits.global
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[(concurrent.ExecutionContext) ?=> Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[(concurrent.ExecutionContext) ?=> Int]):
      |      ((concurrent.ExecutionContext) ?=> Int) | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineContextFunctionTwoContinuationsChained =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedOneLinePrior =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                println("Hello")
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedOneValPrior =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                val w: Int = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(w)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoLinesPrior =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                println("Hello")
      |                println("World")
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoLinesOneValPrior =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                println("Hello")
      |                val w: Int = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(w)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(1)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPrior =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                val w: Int = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(w)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorOneLineBetween =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                val w: Int = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(w)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                println("Hello")
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorOneValBetween =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(b).+(c)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoLinesBetween =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var b: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                val a: Int = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                println("Hello")
      |                println("World")
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(b)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoLinesOneValBetween =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                println("Hello")
      |                val c: Int = a.+(b)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetween =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(b)
      |                val d: Int = c.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c).+(d)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                orThrow
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                $result
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneLineAfter =
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Unit]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Unit]):
      |      Unit | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
      |        var w: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(b)
      |                val d: Int = c.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c).+(d)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                w = orThrow.asInstanceOf[Int]
      |                ()
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                w = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        println(w)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneValAfter =
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Int]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Int]):
      |      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
      |        var w: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(b)
      |                val d: Int = c.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c).+(d)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                w = orThrow.asInstanceOf[Int]
      |                ()
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                w = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        val e: Int = w.+(1)
      |        e:Int
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoLinesAfter =
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Unit]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Unit]):
      |      Unit | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
      |        var w: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(b)
      |                val d: Int = c.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c).+(d)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                w = orThrow.asInstanceOf[Int]
      |                ()
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                w = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        val e: Int = w.+(1)
      |        println(e)
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfter =
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Unit]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Unit]):
      |      Unit | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
      |        var w: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(b)
      |                val d: Int = c.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c).+(d)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                w = orThrow.asInstanceOf[Int]
      |                ()
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                w = $result.asInstanceOf[Int]
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        val e: Int = w.+(1)
      |        val f: Int = z.+(w).+(a)
      |        {
      |          e.+(f)
      |          ()
      |        }
      |      }
      |  }
      |}
      |""".stripMargin

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfterChainIgnored =
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
      |      def I$0_=(x$0: Any): Unit = ()
      |      def I$1_=(x$0: Any): Unit = ()
      |      def I$2_=(x$0: Any): Unit = ()
      |      def I$3_=(x$0: Any): Unit = ()
      |      def I$4_=(x$0: Any): Unit = ()
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
      |          continuations.compileFromString$package.fooTest(null, null,
      |            this.asInstanceOf[continuations.Continuation[Unit]]
      |          )
      |        }
      |    }
      |    def fooTest(x: Int, y: Int, completion: continuations.Continuation[Unit]):
      |      Unit | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
      |     =
      |      {
      |        var x##1: Int = x
      |        var y##1: Int = y
      |        var a: Int = null
      |        var b: Int = null
      |        var z: Int = null
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
      |                $continuation = x$0
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
      |                continuations.Continuation.checkResult($result)
      |                a = 1
      |                b = 1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 1
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](x##1.+(y##1).+(a)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                z = orThrow.asInstanceOf[Int]
      |                return[label1] ()
      |                ()
      |              case 1 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                continuations.Continuation.checkResult($result)
      |                z = $result.asInstanceOf[Int]
      |                label1[Unit]: <empty>
      |                val c: Int = a.+(b)
      |                val d: Int = c.+(1)
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$0 = y##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$1 = x##1
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$2 = a
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$3 = b
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].I$4 = z
      |                $continuation.asInstanceOf[
      |                  continuations.compileFromString$package.
      |                    compileFromString$package$fooTest$1
      |                ].$label = 2
      |                val safeContinuation: continuations.SafeContinuation[Int] =
      |                  new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
      |                    continuations.Continuation.State.Undecided
      |                  )
      |                safeContinuation.resume(Right.apply[Nothing, Int](z.+(c).+(d)))
      |                val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      |                  safeContinuation.getOrThrow()
      |                if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
      |                ()
      |              case 2 =>
      |                y##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$0
      |                x##1 =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$1
      |                a =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$2
      |                b =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$3
      |                z =
      |                  $continuation.asInstanceOf[
      |                    continuations.compileFromString$package.
      |                      compileFromString$package$fooTest$1
      |                  ].I$4
      |                continuations.Continuation.checkResult($result)
      |                ()
      |              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
      |            }
      |        }
      |        val e: Int = z.+(1)
      |        val f: Int = z.+(a)
      |        {
      |          e.+(f)
      |          ()
      |        }
      |      }
      |  }
      |}
      |""".stripMargin
}
