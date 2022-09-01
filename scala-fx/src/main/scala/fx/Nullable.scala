package fx

/**
 * An unwrapped option type. Allows for nullability handling without wrapping the nullable
 * value.
 */
opaque type Nullable[A] = A | Null

extension [A: Manifest](a: Nullable[A])
  /**
   * Alias of value
   */
  def v: Errors[NullPointerException] ?=> A = value

  /**
   * True if the nullable is non-null
   */
  def exists: Boolean =
    a match
      case x: A => true
      case _ => false

  /**
   * @return
   *   true If this nullable contains a value.
   */
  def nonEmpty: Boolean = exists

  /**
   * Unifies the value to A or shifts control to a NullPointerException,
   * @return
   *   the value of a in a NullPointerException context
   */
  def value: Errors[NullPointerException] ?=> A =
    getOrElse(NullPointerException().shift)

  /**
   * @param default
   *   A non-null default value to use if the current value is null.
   * @return
   *   the value as a nullable if the nullable is not null, or a default nullable if it is null.
   */
  def getOrElse(default: A): A =
    a match
      case x: A => x
      case null => default

  /**
   * @param default
   *   A nullable value to use as the default
   * @return
   *   the value as a nullable if the nullable is not null, or a default nullable if it is null.
   */
  def orElse(default: Nullable[A]): Nullable[A] =
    if (exists) a else default

  /**
   * @return
   *   f applied to the value if it exists
   */
  def nullableMap[B](f: A => B): Nullable[B] =
    a match
      case x: A => f(x)
      case _ => Nullable.none

  /**
   * @return
   *   f applied to the value if it exists
   */
  def nullableBind[B](f: A => Nullable[B]): Nullable[B] =
    a match
      case x: A => f(x)
      case _ => null

object Nullable:
  /**
   * Creates a Nullable from a value.
   */
  def apply[A](a: A | Null): Nullable[A] = a

  /**
   * Returns a none
   */
  def none[A]: Nullable[A] = null
