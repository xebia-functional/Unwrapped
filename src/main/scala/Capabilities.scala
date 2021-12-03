package fx

import scala.annotation.showAsInfix

@showAsInfix
infix type *[+R, -E] = Control[Nothing] ?=> E ?=> R

