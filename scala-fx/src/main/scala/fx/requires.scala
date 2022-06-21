package fx

import scala.compiletime.error
import scala.quoted.Expr
import scala.quoted.Quotes
import scala.quoted.Type

/**
 * Super-simple compile time requirements.
 *
 * If the predicate fails, the compile time error message will print as an error.
 *
 * ### Usage
 * {{{
 * import fx.requires
 * opaque type MyInt = Int
 * object MyInt:
 *   def apply(x: Int): MyInt =
 *     require(x < 5, "MyInt must be less than 5")
 *     5
 * MyInt(3) // compiles
 * MyInt(7) // fails compilation with "MyInt must be less than 5"
 * }}}
 */
inline def requires[A](
    inline assertion: Boolean,
    inline errorMessage: String,
    inline value: A): A =
  ${ requiresImpl('assertion, 'errorMessage, 'value) }

private[fx] def requiresImpl[A](
    assertionExpression: Expr[Boolean],
    errorMessageExpression: Expr[String],
    valueExpression: Expr[A])(using Type[A])(using Quotes): Expr[A] =
  '{
    if ! $assertionExpression then error($errorMessageExpression)
    $valueExpression
  }
