package examples

import continuations.*
import continuations.jvm.internal.ContinuationImpl

@main def main =
//  val result: Int = program
//  println(result)
  val result2 = programSuspendContinuationNoParamResume
  println(result2)
  val result3 = programSuspendContinuationNoParamNoSuspendContinuation
  println(result3)
  val result4 = programSuspendContinuationNoParamResumeIgnoreResult
  println(result4)
  println(programSuspendContinuationParamDependent)
  println(programSuspendContinuationResumeVals)
  println(programOneContinuationReturnValue)
  // println(programSuspendContinuationNoSuspendContinuationVal)
  println(ExampleObject.continuations(1, 2))
  println(programStartContinuation)

object main$handler extends (Continuation[Any | Null] => Any):
  override def apply($completion: Continuation[Any | Null]): Any =
    main$expanded(using $completion)

class main$continuation$1($completion: Continuation[Any | Null])
    extends ContinuationImpl($completion, $completion.context) {
  var result: Any = ???
  var label: Int = ???

  final def invokeSuspend(
      result: Either[Throwable, Any | Null | Continuation.State.Suspended.type]): Any | Null =
    this.result = result
    this.label |= Integer.MIN_VALUE
    main$expanded(using this)
}

def main$expanded(
    using var0: Continuation[Any | Null]): Any | Null | Continuation.State.Suspended.type =
  val $continuation: main$continuation$1 =
    if (var0.isInstanceOf[main$continuation$1])
      if ((var0.asInstanceOf[main$continuation$1].label & Integer.MIN_VALUE) != 0)
        var0.asInstanceOf[main$continuation$1].label -= Integer.MIN_VALUE
      var0.asInstanceOf[main$continuation$1]
    else new main$continuation$1(var0)
  val $result = $continuation.result
  val var6 = Continuation.State.Suspended
  var var10000: Any | Null = null
  ($continuation.label) match
    case 0 =>
      if ($result.isInstanceOf[Left[_, _]])
        val leftValue = $continuation.result.asInstanceOf[Left[_, _]].value
        if (leftValue.isInstanceOf[Throwable]) throw leftValue.asInstanceOf[Throwable]
      $continuation.label = 1
      var10000 = program$expanded(using $continuation)
      if (var10000 == var6) return var6
    case 1 =>
      if ($result.isInstanceOf[Left[_, _]])
        val leftValue = $continuation.result.asInstanceOf[Left[_, _]].value
        if (leftValue.isInstanceOf[Throwable]) throw leftValue.asInstanceOf[Throwable]
      var10000 = $result
    case _ =>
      throw new IllegalStateException("call to `resume` before continuation is ready")

  val var1: Int = var10000.asInstanceOf[Number].intValue
  println(var1)
  ()
