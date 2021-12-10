package fx

import scala.annotation.showAsInfix

@showAsInfix
infix type *[+Result, -Effect] = Control[Nothing] ?=> Effect ?=> Result

