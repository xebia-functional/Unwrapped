package fx

opaque type MediaType = String

object MediaType:
  extension (m: MediaType)
    def value: String =
      m

  def apply(s: String): MediaType =
    s
