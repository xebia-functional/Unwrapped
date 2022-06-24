package fx

opaque type HttpsClient[A] = Https ?=> A

object HttpsClient {
  def apply[A](a: A): HttpsClient[A] = a
}
