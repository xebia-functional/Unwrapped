package sttp
package unwrapped

import _root_.unwrapped.{given, *}
import java.net.URI
import java.net.http.HttpResponse

/**
 * Sttp's BodyFromResponseAs can only really work with responses bound to a statically known
 * response body type, which must be some kind of byte array or convertible to a byte array. To
 * maintain the capability of Http of serving any type of response with body type inference, we
 * are using [Rob Norris's Kinda-Curried Type
 * Parameters](https://tpolecat.github.io/2015/07/30/infer.html), accessed
 * 2022/08/11T23:01:00.000-5:00.
 */
extension (uri: URI)

  private[unwrapped] def patch[A] = PatchPartiallyApplied[A](uri)

  private[unwrapped] def post[A] = PostPartiallyApplied[A](uri)

  private[unwrapped] def put[A] = PutPartiallyApplied[A](uri)
