package continuations {
  final lazy module val compileFromStringpackage:
    continuations.compileFromStringpackage
   = new continuations.compileFromStringpackage()
  @SourceFile("compileFromStringscala") final module class
    compileFromStringpackage
  () extends Object() { this: continuations.compileFromStringpackage.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromStringpackage.type])
    def foo(x: Int, completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type =
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
