assertEqualsFX[R, A, B](obtained: (fx.Control[R]) ?=> A, expected: B, clue: => Any): (munit.Location) ?=> (B <:< A) ?=> (fx.Control[AssertionError], fx.Control[R]) 
  ?=>
 Unit
Lifts munit.Assertions#assertEquals into Errors for testing. Will not return until given an
Error[AssertionError], typically within run or structured.


Type Parameters
- A: 
  the type of the obtained value
- B: 
  the type of the expected value

Parameters
- obtained: 
  The actual value under test

See
- [
  [munit.Clue]](
  [munit.Clue])
