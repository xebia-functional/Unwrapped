package fx

/**
 * Models http status codes as Integers
 */
opaque type StatusCode = Int

object StatusCode {
  extension (s: StatusCode) {
    def value: Int = s
    def statusText = s match {
      case Continue => "Continue"
      case `Switching Protocols` => "Switching Protocols"
      case Processing => "Processing"
      case `Early Hints` => "Early Hints"
      case OK => "OK"
      case Created => "Created"
      case Accepted => "Accepted"
      case `Non-Authoritative Information` => "Non-Authoritative Information"
      case `No-Content` => "No-Content"
      case `Reset Content` => "Reset Content"
      case `Partial Content` => "Partial Content"
      case `Multi-Status` => "Multi-Status"
      case `Already Reported` => "Already Reported"
      case `IM Used` => "IM Used"
      case `Multiple Choices` => "Multiple Choices"
      case `Moved Permanently` => "Moved Permanently"
      case `Found` => "Found"
      case `See Other` => "See Other"
      case `Not Modified` => "Not Modified"
      case `Use Proxy` => "Use Proxy"
      case `Temporary Redirect` => "Temporary Redirect"
      case `Permanent Redirect` => "Permanent Redirect"
      case `Bad Request` => "Bad Request"
      case Unauthorized => "Unauthorized"
      case `Payment Required` => "Payment Required"
      case Forbidden => "Forbidden"
      case `Not Found` => "Not Found"
      case `Method Not Allowed` => "Method Not Allowed"
      case `NotAcceptable` => "NotAcceptable"
      case `Proxy Authentication Required` => "Proxy Authentication Required"
      case `Request Timeout` => "Request Timeout"
      case `Conflict` => "Conflict"
      case `Gone` => "Gone"
      case `Length Required` => "Length Required"
      case `Precondition Failed` => "Precondition Failed"
      case `Payload Too Large` => "Payload Too Large"
      case `URI Too Long` => "URI Too Long"
      case `Unsupported Media Type` => "Unsupported Media Type"
      case `Range Not Satisfiable` => "Range Not Satisfiable"
      case `Expectation Failed` => "Expectation Failed"
      case `I'm a teapot` => "I'm a teapot"
      case `Misdirected Request` => "Misdirected Request"
      case `Unprocessable Entity` => "Unprocessable Entity"
      case `Locked` => "Locked"
      case `Failed Dependency` => "Failed Dependency"
      case `Too Early` => "Too Early"
      case `Upgrade Required` => "Upgrade Required"
      case `Precondition Required` => "Precondition Required"
      case `Too Many Requests` => "Too Many Requests"
      case `Request Header Fields Too Large` => "Request Header Fields Too Large"
      case `Unavailable For Legal Reasons` => "Unavailable For Legal Reasons"
      case `Internal Server Error` => "Internal Server Error"
      case `Not Implemented` => "Not Implemented"
      case `Bad Gateway` => "Bad Gateway"
      case `Service Unavailable` => "Service Unavailable"
      case `Gateway Timeout` => "Gateway Timeout"
      case `Http Version Not Supported` => "Http Version Not Supported"
      case `Variant Also Negotiates` => "Variant Also Negotiates"
      case `Insufficient Storage` => "Insufficient Storage"
      case `Loop Detected` => "Loop Detected"
      case `Not Extended` => "Not Extended"
      case `Network Authentication Required` => "Network Authentication Required"
      case unused => "unused"

    }
  }

  def statusCodes =
    (100 to 103).toSet ++ (200 to 208).toSet + 226 ++ (300 to 308).toSet ++ (400 to 418).toSet ++ (421 to 426).toSet ++ Set(
      428,
      429,
      431,
      451) ++ (500 to 511).toSet

  private[this] val predicateError = "status code must be a valid status code"

  inline def apply(i: Int): StatusCode =
    requires(
      (i >= 100 && i <= 103) ||
        (i >= 200 && i <= 208) ||
        i == 226 ||
        (i >= 300 && i <= 308) ||
        (i >= 400 && i <= 418) ||
        (i >= 421 && i <= 426) ||
        i == 428 ||
        i == 429 ||
        i == 431 ||
        i == 451 ||
        (i >= 500 && i <= 511),
      "status code must be a valid status code",
      i
    )
  def of(i: Int): Errors[String] ?=> StatusCode =
    if (statusCodes.contains(i))
      i
    else
      predicateError.shift

  def unsafeOf(i: Int): StatusCode =
    assert(statusCodes.contains(i))
    i
}
