package fx

type HttpsClient[A] = Https[A] ?=> A

object HttpsClient {
  def apply[A](a: A): HttpsClient[A] = a
}
