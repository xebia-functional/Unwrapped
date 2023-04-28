package unwrapped

type HttpHeader = (String, ::[String])

extension (header: HttpHeader)
  def name: String = header._1
  def values: ::[String] = header._2

object HttpHeader:
  def apply(header: (String, ::[String])): HttpHeader =
    header

  def apply(headerName: String, value: String, values: String*): HttpHeader =
    headerName -> ::(value, values.toList)
