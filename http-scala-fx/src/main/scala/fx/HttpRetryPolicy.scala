package fx

import java.net.http.HttpResponse

type HttpRetryPolicy =
  HttpResponse[?] => Boolean

extension (a: HttpResponse[?])
  def shouldRetry: HttpRetryPolicy ?=> Boolean = summon[HttpRetryPolicy](a)

object HttpRetryPolicy:
  def apply(f: HttpResponse[_] => Boolean): HttpRetryPolicy =
    f

  given defaultRetryPolicy: HttpRetryPolicy =
    r => (400 to 499).contains(r.statusCode)
