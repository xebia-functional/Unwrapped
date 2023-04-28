package unwrapped

/**
 * Models the filename part of a multipart part.
 */
opaque type PartSpecificationFilename = String

object PartSpecificationFilename:
  def apply(s: String): PartSpecificationFilename = s

  /**
   * Gets the string value of this filename
   */
  extension (p: PartSpecificationFilename) def value: String = p
