package fx

opaque type HttpsClient[A] = A

object HttpsClient {
  def apply[A](a: A): HttpsClient[A] = a
}
