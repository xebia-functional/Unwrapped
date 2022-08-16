package sttp
package fx

import _root_.fx.*
import sttp.{model => sm}

trait StatusCodeToStatusCode[A, B]:
  extension (a: A) def toStatusCode: Http[B]

object StatusCodeToStatusCode{
  def apply[A, B](using StatusCodeToStatusCode[A, B]): StatusCodeToStatusCode[A, B] =
    summon


  given StatusCodeToStatusCode[Int, sm.StatusCode] with
    extension (a: Int)
      def toStatusCode: Http[sm.StatusCode] =
        sm.StatusCode
          .safeApply(a)
          .fold(s => HttpExecutionException(new RuntimeException(s)).shift, identity)

  given StatusCodeToStatusCode[StatusCode, sm.StatusCode] with
    extension (a: StatusCode)
      def toStatusCode: Http[sm.StatusCode] =
        a match
          case Continue => sm.StatusCode.Continue
          case `Switching Protocols` => sm.StatusCode.SwitchingProtocols
          case Processing => sm.StatusCode.Processing
          case `Early Hints` => sm.StatusCode.EarlyHints
          case OK => sm.StatusCode.Ok
          case Created => sm.StatusCode.Created
          case Accepted => sm.StatusCode.Accepted
          case `Non-Authoritative Information` => sm.StatusCode.NonAuthoritativeInformation
          case `No-Content` => sm.StatusCode.NoContent
          case `Reset Content` => sm.StatusCode.ResetContent
          case `Partial Content` => sm.StatusCode.PartialContent
          case `Multi-Status` => sm.StatusCode.MultiStatus
          case `Already Reported` => sm.StatusCode.AlreadyReported
          case `IM Used` => sm.StatusCode.ImUsed
          case `Multiple Choices` => sm.StatusCode.MultipleChoices
          case `Moved Permanently` => sm.StatusCode.MovedPermanently
          case `Found` => sm.StatusCode.Found
          case `See Other` => sm.StatusCode.SeeOther
          case `Not Modified` => sm.StatusCode.NotModified
          case `Use Proxy` => sm.StatusCode.UseProxy
          case `Temporary Redirect` => sm.StatusCode.TemporaryRedirect
          case `Permenant Redirect` => sm.StatusCode.PermanentRedirect
          case `Bad Request` => sm.StatusCode.BadRequest
          case Unauthorized => sm.StatusCode.Unauthorized
          case `Payment Required` => sm.StatusCode.PaymentRequired
          case Forbidden => sm.StatusCode.Forbidden
          case `Not Found` => sm.StatusCode.NotFound
          case `Method Not Allowed` => sm.StatusCode.MethodNotAllowed
          case `NotAcceptable` => sm.StatusCode.NotAcceptable
          case `Proxy Authentication Required` => sm.StatusCode.ProxyAuthenticationRequired
          case `Request Timeout` => sm.StatusCode.RequestTimeout
          case `Conflict` => sm.StatusCode.Conflict
          case `Gone` => sm.StatusCode.Gone
          case `Length Required` => sm.StatusCode.LengthRequired
          case `Precondition Failed` => sm.StatusCode.PreconditionFailed
          case `Payload Too Large` => sm.StatusCode.PayloadTooLarge
          case `URI Too Long` => sm.StatusCode.UriTooLong
          case `Unsupported Media Type` => sm.StatusCode.UnsupportedMediaType
          case `Range Not Satisfiable` => sm.StatusCode.RangeNotSatisfiable
          case `Expectation Failed` => sm.StatusCode.ExpectationFailed
          case `Misdirected Request` => sm.StatusCode.MisdirectedRequest
          case `Unprocessable Entity` => sm.StatusCode.UnprocessableEntity
          case `Locked` => sm.StatusCode.Locked
          case `Failed Dependency` => sm.StatusCode.FailedDependency
          case `Upgrade Required` => sm.StatusCode.UpgradeRequired
          case `Precondition Required` => sm.StatusCode.PreconditionRequired
          case `Too Many Requests` => sm.StatusCode.TooManyRequests
          case `Request Header Fields Too Large` => sm.StatusCode.RequestHeaderFieldsTooLarge
          case `Unavailable For Legal Reasons` => sm.StatusCode.UnavailableForLegalReasons
          case `Internal Server Error` => sm.StatusCode.InternalServerError
          case `Not Implemented` => sm.StatusCode.NotImplemented
          case `Bad Gateway` => sm.StatusCode.BadGateway
          case `Service Unavailable` => sm.StatusCode.ServiceUnavailable
          case `Gateway Timeout` => sm.StatusCode.GatewayTimeout
          case `Http Version Not Supported` => sm.StatusCode.HttpVersionNotSupported
          case `Variant Also Negotiates` => sm.StatusCode.VariantAlsoNegotiates
          case `Insufficient Storage` => sm.StatusCode.InsufficientStorage
          case `Loop Detected` => sm.StatusCode.LoopDetected
          case `Not Extended` => sm.StatusCode.NotExtended
          case `Network Authentication Required` => sm.StatusCode.NetworkAuthenticationRequired
          case _ =>
            sm.StatusCode
              .safeApply(unused.value)
              .fold(err => HttpExecutionException(new RuntimeException(err)).shift, identity)

  given smStatusCode:StatusCodeToStatusCode[sm.StatusCode, StatusCode] with
    extension (a: sm.StatusCode)
      def toStatusCode: Http[StatusCode] =
        a match
          case sm.StatusCode.Continue => Continue
          case sm.StatusCode.SwitchingProtocols => `Switching Protocols`
          case sm.StatusCode.Processing => Processing
          case sm.StatusCode.EarlyHints => `Early Hints`
          case sm.StatusCode.Ok => OK
          case sm.StatusCode.Created => Created
          case sm.StatusCode.Accepted => Accepted
          case sm.StatusCode.NonAuthoritativeInformation => `Non-Authoritative Information`
          case sm.StatusCode.NoContent => `No-Content`
          case sm.StatusCode.ResetContent => `Reset Content`
          case sm.StatusCode.PartialContent => `Partial Content`
          case sm.StatusCode.MultiStatus => `Multi-Status`
          case sm.StatusCode.AlreadyReported => `Already Reported`
          case sm.StatusCode.ImUsed => `IM Used`
          case sm.StatusCode.MultipleChoices => `Multiple Choices`
          case sm.StatusCode.MovedPermanently => `Moved Permanently`
          case sm.StatusCode.Found => `Found`
          case sm.StatusCode.SeeOther => `See Other`
          case sm.StatusCode.NotModified => `Not Modified`
          case sm.StatusCode.UseProxy => `Use Proxy`
          case sm.StatusCode.TemporaryRedirect => `Temporary Redirect`
          case sm.StatusCode.PermanentRedirect => `Permenant Redirect`
          case sm.StatusCode.BadRequest => `Bad Request`
          case sm.StatusCode.Unauthorized => Unauthorized
          case sm.StatusCode.PaymentRequired => `Payment Required`
          case sm.StatusCode.Forbidden => Forbidden
          case sm.StatusCode.NotFound => `Not Found`
          case sm.StatusCode.MethodNotAllowed => `Method Not Allowed`
          case sm.StatusCode.NotAcceptable => `NotAcceptable`
          case sm.StatusCode.ProxyAuthenticationRequired => `Proxy Authentication Required`
          case sm.StatusCode.RequestTimeout => `Request Timeout`
          case sm.StatusCode.Conflict => `Conflict`
          case sm.StatusCode.Gone => `Gone`
          case sm.StatusCode.LengthRequired => `Length Required`
          case sm.StatusCode.PreconditionFailed => `Precondition Failed`
          case sm.StatusCode.PayloadTooLarge => `Payload Too Large`
          case sm.StatusCode.UriTooLong => `URI Too Long`
          case sm.StatusCode.UnsupportedMediaType => `Unsupported Media Type`
          case sm.StatusCode.RangeNotSatisfiable => `Range Not Satisfiable`
          case sm.StatusCode.ExpectationFailed => `Expectation Failed`
          case sm.StatusCode.MisdirectedRequest => `Misdirected Request`
          case sm.StatusCode.UnprocessableEntity => `Unprocessable Entity`
          case sm.StatusCode.Locked => `Locked`
          case sm.StatusCode.FailedDependency => `Failed Dependency`
          case sm.StatusCode.UpgradeRequired => `Upgrade Required`
          case sm.StatusCode.PreconditionRequired => `Precondition Required`
          case sm.StatusCode.TooManyRequests => `Too Many Requests`
          case sm.StatusCode.RequestHeaderFieldsTooLarge => `Request Header Fields Too Large`
          case sm.StatusCode.UnavailableForLegalReasons => `Unavailable For Legal Reasons`
          case sm.StatusCode.InternalServerError => `Internal Server Error`
          case sm.StatusCode.NotImplemented => `Not Implemented`
          case sm.StatusCode.BadGateway => `Bad Gateway`
          case sm.StatusCode.ServiceUnavailable => `Service Unavailable`
          case sm.StatusCode.GatewayTimeout => `Gateway Timeout`
          case sm.StatusCode.HttpVersionNotSupported => `Http Version Not Supported`
          case sm.StatusCode.VariantAlsoNegotiates => `Variant Also Negotiates`
          case sm.StatusCode.InsufficientStorage => `Insufficient Storage`
          case sm.StatusCode.LoopDetected => `Loop Detected`
          case sm.StatusCode.NotExtended => `Not Extended`
          case sm.StatusCode.NetworkAuthenticationRequired =>
            `Network Authentication Required`
          case x: sm.StatusCode if x.code == 306 => unused
          case x: sm.StatusCode if x.code == 418 => `I'm a teapot`
          case x: sm.StatusCode if x.code == 425 => `Too Early`
          case _ =>
            HttpExecutionException(new RuntimeException("Unrecognized Status Code")).shift
}
