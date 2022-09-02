package fx

/**
 * Models HttpRetries as an Int
 */
opaque type HttpRetries = Int

object HttpRetries:

  given Manifest[HttpRetries] =
    Manifest.classType[HttpRetries](HttpRetries(1).getClass)

  /**
   * @constructor
   */
  inline def apply(i: Int): HttpRetries =
    requires(i >= 0, "HttpRetries must be greater than 0", i)

  /**
   * Safe constructor for runtime instantiation
   */
  def of(i: Int): Errors[HttpExecutionException] ?=> HttpRetries =
    if (i >= 0)
      i
    else
      HttpExecutionException(RuntimeException("HttpRetries must be greater than 0")).shift

  extension (h: HttpRetries)
    /**
     * @return
     *   The int value of the HttpRetries object
     */
    def value: Int = h

    /**
     * @param z
     *   The HttpRetries to add to this Http retries
     * @return
     *   The HttpRetries added to this Http retries
     */
    def +(z: HttpRetries): HttpRetries = value + z.value

  given HttpRetries =
    HttpRetries(3)
