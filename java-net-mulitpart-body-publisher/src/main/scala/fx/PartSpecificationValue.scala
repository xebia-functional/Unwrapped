package unwrapped

/**
 * Models the value of a multipart part.
 */
opaque type PartSpecificationValue = String

object PartSpecificationValue:
  def apply(s: String): PartSpecificationValue = s

  /**
   * Gets the string value of this value.
   */
  extension (p: PartSpecificationValue) def value: String = p
