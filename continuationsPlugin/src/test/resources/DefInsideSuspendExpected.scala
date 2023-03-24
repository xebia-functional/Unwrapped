package continuations {
  final lazy module val compileFromString$package:
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    def foo(x: Int)(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type =
      {
        val continuation1: continuations.Continuation[Int] = completion
        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
        {
          {
            def test(x: Int): Int = x.+(1)
            safeContinuation.resume(test(1).+(1))
          }
        }
        safeContinuation.getOrThrow()
      }
  }
}
