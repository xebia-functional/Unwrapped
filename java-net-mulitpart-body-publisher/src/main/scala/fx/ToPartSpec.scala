package fx

/**
 * Provides a conversion from a PartSpecification to an array of bytes for http request
 * publishing.
 */
trait ToPartSpec[A <: PartSpecification]:
  extension (p: A) def toPartSpec: Array[Byte]
