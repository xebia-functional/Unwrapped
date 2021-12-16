package fx

import scala.annotation.showAsInfix

@showAsInfix
infix type %[+Result, -Effect] = Effect ?=> Result
