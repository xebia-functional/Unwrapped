package continuations {
  final lazy module val compileFromStringpackage: 
    continuations.compileFromStringpackage
   = new continuations.compileFromStringpackage()
  @SourceFile("compileFromStringscala") final module class 
    compileFromStringpackage
  () extends Object() { this: continuations.compileFromStringpackage.type =>
    private def writeReplace(): AnyRef = 
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromStringpackage.type])
    def foo(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
      {
        val continuation1: continuations.Continuation[Int] = completion
        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
        {
          {
            safeContinuation.resume(1)
          }
        }
        safeContinuation.getOrThrow()
      }
  }
}

