package fx

opaque type |?[A] = A | Null

extension [A](a: |?[A])
  def value: A | Null = a
  def getOrElse(b: A) = b
  def orElse(b: |?[A]) = b
  def map[B](f: A => B): |?[B] =
    value match
      case x: A => f(x)
      case _ => null
  def bind[B](f: A => |?[B]): |?[B] =
    value match
      case x: A => f(x)
      case _ => null

object |?{
  def apply[A](a: A | Null): |?[A] = a
  def none[A]: |?[A] = null
}
