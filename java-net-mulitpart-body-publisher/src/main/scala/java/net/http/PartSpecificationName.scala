package java
package net
package http

/**
 * Models the name of this multipart part.
 */
opaque type PartSpecificationName = String

object PartSpecificationName:
  def apply(s: String): PartSpecificationName = s

  /**
   * Gets the string value of this name.
   */
  extension (p: PartSpecificationName) def value: String = p
