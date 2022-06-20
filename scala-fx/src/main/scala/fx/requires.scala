package fx

import scala.compiletime.error
import scala.quoted.Expr
import scala.quoted.Quotes

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
inline def requires[A](inline assertion: Boolean, inline errorMessage: String): Unit =
  ${ requiresImpl('assertion, 'errorMessage) }

private[fx] def requiresImpl(
    assertionExpression: Expr[Boolean],
    errorMessageExpression: Expr[String])(using Quotes): Expr[Unit] =
  '{
    if ! $assertionExpression then error($errorMessageExpression)
  }
