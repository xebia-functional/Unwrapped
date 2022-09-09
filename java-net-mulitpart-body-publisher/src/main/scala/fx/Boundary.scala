package fx

/**
 * Represents a multipart boundary
 */
opaque type Boundary = String

object Boundary:
  /**
   * Gets the string value out of the boundary opaque type
   */
  extension (b: Boundary) def value: String = b

  /**
   * Creates a multi-part boundary from a string.
   */
  def apply(s: String): Boundary =
    s
