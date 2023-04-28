package unwrapped

import scala.compiletime.error
import scala.quoted.*

/**
 * Super-simple compile time requirements.
 *
 * If the predicate fails, the compile time error message will print as an error.
 *
 * ### Usage
 * {{{
 * import unwrapped.requires
 * opaque type MyInt = Int
 * object MyInt:
 *   def apply(x: Int): MyInt =
 *     require(x < 5, "MyInt must be less than 5")
 *     5
 * MyInt(3) // compiles
 * MyInt(7) // fails compilation with "MyInt must be less than 5"
 * }}}
 */
inline def requires[A](assertion: Boolean, errorMessage: String, value: A): A = {
  ${ requiresImpl('assertion, 'errorMessage, 'value) }
}

private[unwrapped] def requiresImpl[A](
    assertionExpression: Expr[Boolean],
    errorMessageExpression: Expr[String],
    value: Expr[A])(using Quotes)(using Type[A]): Expr[A] =
  '{
    if $assertionExpression == false then error($errorMessageExpression) else $value
  }
