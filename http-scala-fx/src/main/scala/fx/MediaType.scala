package fx

opaque type MediaType = String

extension (m: MediaType)
  def value: String =
    m

object MediaType:
  def apply(s: String): MediaType =
    s
