package continuations {
  final lazy module val compileFromString$package: 
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class 
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef = 
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    def foo(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
      {
        val continuation1: continuations.Continuation[Int] = completion
        val safeContinuation: continuations.SafeContinuation[Int] = 
          new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
            continuations.Continuation.State.Undecided
          )
        {
          {
            {
              safeContinuation.resume(Right.apply[Nothing, Int](1))
            }
          }
        }
        safeContinuation.getOrThrow()
      }
  }
}