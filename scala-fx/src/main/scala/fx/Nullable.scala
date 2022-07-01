package fx

opaque type |?[A] = A | Null

extension [A: Manifest](a: |?[A])
  def getOrElse(b: A) = b
  def orElse(b: |?[A]) =
    a match
      case x: A => a
      case _ => b
  def map[B](f: A => B): |?[B] =
    a match
      case x: A => f(x)
      case _ => null
  def bind[B](f: A => |?[B]): |?[B] =
    a match
      case x: A => f(x)
      case _ => null

object |? {
  def apply[A](a: A | Null): |?[A] = a
  def none[A]: |?[A] = null
}
