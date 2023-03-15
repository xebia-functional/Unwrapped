package continuations {
  @SourceFile("compileFromString.scala") case class Foo(x: Int) extends Object(), _root_.scala.Product, _root_.
    scala
  .Serializable {
    override def hashCode(): Int =
      {
        var acc: Int = -889275714
        acc = scala.runtime.Statics#mix(acc, this.productPrefix.hashCode())
        acc = scala.runtime.Statics#mix(acc, Foo.this.x)
        scala.runtime.Statics#finalizeHash(acc, 1)
      }
    override def equals(x$0: Any): Boolean =
      this.eq(x$0.$asInstanceOf[Object]).||(
        x$0 match
          {
            case x$0 @ _:continuations.Foo @unchecked => this.x.==(x$0.x).&&(x$0.canEqual(this))
            case _ => false
          }
      )
    override def toString(): String = scala.runtime.ScalaRunTime._toString(this)
    override def canEqual(that: Any): Boolean = that.isInstanceOf[continuations.Foo @unchecked]
    override def productArity: Int = 1
    override def productPrefix: String = "Foo"
    override def productElement(n: Int): Any =
      n match
        {
          case 0 => this._1
          case _ => throw new IndexOutOfBoundsException(n.toString())
        }
    override def productElementName(n: Int): String =
      n match
        {
          case 0 => "x"
          case _ => throw new IndexOutOfBoundsException(n.toString())
        }
    val x: Int
    def copy(x: Int): continuations.Foo = new continuations.Foo(x)
    def copy$default$1: Int @uncheckedVariance = Foo.this.x
    def _1: Int = this.x
  }
  final lazy module val Foo: continuations.Foo = new continuations.Foo()
  @SourceFile("compileFromString.scala") final module class Foo() extends AnyRef(), scala.deriving.Mirror.
    Product
   { this: continuations.Foo.type =>
    private def writeReplace(): AnyRef = new scala.runtime.ModuleSerializationProxy(classOf[continuations.Foo.type])
    def apply(x: Int): continuations.Foo = new continuations.Foo(x)
    def unapply(x$1: continuations.Foo): continuations.Foo = x$1
    override def toString: String = "Foo"
    type MirroredMonoType = continuations.Foo
    def fromProduct(x$0: Product): continuations.Foo.MirroredMonoType = new continuations.Foo(x$0.productElement(0).$asInstanceOf[Int])
  }
  final lazy module val compileFromString$package:
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    def program: continuations.Foo =
      {
        class program$fooTest$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion
          , 
        $completion.context) {
          var I$0: Any = _
          var I$1: Any = _
          var I$2: Any = _
          def I$0_=(x$0: Any): Unit = ()
          def I$1_=(x$0: Any): Unit = ()
          def I$2_=(x$0: Any): Unit = ()
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
              fooTest(null, null, this)
            }
          override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] =
            new continuations.jvm.internal.BaseContinuationImpl(completion)
          protected def invoke(p1: Any | Null, p2: continuations.Continuation[Any | Null]): Any | Null =
            this.create(p1, p2).asInstanceOf[(BaseContinuationImpl.this : continuations.jvm.internal.BaseContinuationImpl)].invokeSuspend(
              new Right[Unit, Unit](())
            )
        }
        def fooTest(a: A, b: Int, completion: continuations.Continuation[A]):
          A | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
         =
          {
            var a##1: A = a
            var b##1: Int = b
            var z: A = null
            {
              val $continuation: program$fooTest$1 = 
                completion match
                  {
                    case x$0 @ x$0:program$fooTest$1 if x$0.$label.&(scala.Int.MinValue).!=(0) =>
                      x$0.$label = x$0.$label.-(scala.Int.MinValue)
                      x$0
                    case _ => new program$fooTest$1(completion)
                  }
              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] =
                $continuation.$result
              $continuation.$label match
                {
                  case 0 =>
                    continuations.Continuation.checkResult($result)
                    $continuation.I$0 = b##1
                    $continuation.I$1 = a##1
                    $continuation.$label = 1
                    val safeContinuation: continuations.SafeContinuation[A] = continuations.SafeContinuation.init[A]($continuation)
                    {
                      safeContinuation.resume(a##1)
                    }
                    safeContinuation.getOrThrow() match
                      {
                        case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                        case orThrow @ <empty> =>
                          z = orThrow.asInstanceOf[A]
                          return[label1] ()
                      }
                  case 1 =>
                    b##1 = $continuation.I$0
                    a##1 = $continuation.I$1
                    continuations.Continuation.checkResult($result)
                    z = $result.asInstanceOf[A]
                    label1[Unit]: <empty>
                    $continuation.I$0 = b##1
                    $continuation.I$1 = a##1
                    $continuation.I$2 = z
                    $continuation.$label = 2
                    val safeContinuation: continuations.SafeContinuation[A] = continuations.SafeContinuation.init[A]($continuation)
                    {
                      safeContinuation.resume(
                        {
                          println("World")
                          z
                        }
                      )
                    }
                    safeContinuation.getOrThrow() match
                      {
                        case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                        case orThrow @ <empty> => orThrow
                      }
                  case 2 =>
                    b##1 = $continuation.I$0
                    a##1 = $continuation.I$1
                    z = $continuation.I$2
                    continuations.Continuation.checkResult($result)
                    $result
                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
                }
            }
          }
        fooTest(continuations.Foo.apply(1), 1, continuations.jvm.internal.ContinuationStub.contImpl)
      }
  }
}
