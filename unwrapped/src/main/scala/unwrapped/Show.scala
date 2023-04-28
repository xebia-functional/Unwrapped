package unwrapped

/**
 * The capability to print a formatted version of some type. Modeling
 *
 * @tparam A
 *   The type that needs printing
 */
opaque type Show[-A] = A => String

/**
 * @tparam A
 *   The type to print
 */
extension [A](f: Show[A])
  /**
   * Prints a value of type A
   */
  def show(a: A): String = f(a)

object Show:
  /**
   * @constructor
   */
  def apply[A](f: A => String): Show[A] = f

  /**
   * Default implementation is a simple to string
   */
  given defaultShow[A]: Show[A] = a => a.toString
