package sttp.unwrapped

import _root_.unwrapped.{given, *}
import java.net.URI
import java.net.http.HttpResponse
import scala.reflect.TypeTest
import scala.concurrent.duration.Duration

/**
 * Sttp's BodyFromResponseAs can only really work with responses bound to a statically known
 * response body type, which must be some kind of byte array or convertible to a byte array. To
 * maintain the capability of Http of serving any type of response with body type inference, we
 * are using [Rob Norris's Kinda-Curried Type
 * Parameters](https://tpolecat.github.io/2015/07/30/infer.html), accessed
 * 2022/08/11T23:01:00.000-5:00.
 *
 * @tparam A
 *   The type of the response to be returned when apply is called.
 */
private[unwrapped] class PatchPartiallyApplied[A](private val uri: URI) extends Equals:
  /**
   * Applies the Http effect, returning the HttpResponse
   *
   * @tparam B
   *   The type of the body pased to the request.
   * @param body
   *   The body of the request.
   * @param headels
   *   Variable-length list of headers to send with the request.
   * @return
   *   The http response in an Http effect.
   */
  def apply[B](body: B, timeout: Duration, headers: HttpHeader*)(
      using HttpResponseMapper[A],
      HttpBodyMapper[B]): Http[HttpResponse[A]] =
    uri.PATCH[A, B](body, timeout, headers: _*)

  override def canEqual(that: Any): Boolean =
    that.isInstanceOf[PatchPartiallyApplied[A]] // cannot disable unchecked warning

  override def equals(x: Any): Boolean =
    canEqual(x) && x.asInstanceOf[PatchPartiallyApplied[A]].uri == uri
