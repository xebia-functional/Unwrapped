package fx

import java.io.InputStream

/**
 * Models the input stream supplier for a multi-part part.
 */
opaque type PartSpecificationInputStream = () => InputStream

object PartSpecificationInputStream:
  def apply(i: () => InputStream): PartSpecificationInputStream = i
  def apply(i: InputStream): PartSpecificationInputStream = () => i

  /**
   * Gets the input stream supplier value for this input stream specification.
   */
  extension (p: PartSpecificationInputStream) def value: () => InputStream = p
