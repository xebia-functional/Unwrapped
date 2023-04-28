package unwrapped

type HttpExecutionException = Exception

object HttpExecutionException {
  def apply(a: Exception): HttpExecutionException = a
}
