package java
package net
package http

/**
 * Models the content type for a multipart part.
 */
opaque type PartSpecificationContentType = String

object PartSpecificationContentType:
  def apply(s: String): PartSpecificationContentType = s

  /**
   * Gets the string value out of the opaque type.
   */
  extension (p: PartSpecificationContentType) def value: String = p
