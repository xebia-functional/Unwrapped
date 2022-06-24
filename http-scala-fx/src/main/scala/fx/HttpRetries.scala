package fx

/**
 * Models HttpRetries as an Int
 */
opaque type HttpRetries = Int

object HttpRetries:

  /**
   * @constructor
   */
  inline def apply(i: Int): HttpRetries =
    requires(i > 0, "HttpRetries must be a positive number", i)

  /**
   * Safe constructor for runtime instantiation
   */
  def of(i: Int): Errors[String] ?=> HttpRetries =
    if (i > 0)
      i
    else
      "HttpRetries must be a positive Number".shift

  extension (h: HttpRetries)
    /**
     * @return
     *   The int value of the HttpRetries object
     */
    def value: Int = h

  given HttpRetries =
    HttpRetries(3)
