package fx

import java.net.http.HttpClient.Redirect

opaque type HttpFollowRedirects = Redirect


object HttpFollowRedirects:
  val always: HttpFollowRedirects = Redirect.ALWAYS
  val never: HttpFollowRedirects = Redirect.NEVER
  val normal: HttpFollowRedirects = Redirect.NORMAL
  given HttpFollowRedirects = normal
  extension(h: HttpFollowRedirects)
    def value: Redirect = h
