package fx

/**
 * Gives the ability for overrideable external string formatting of any object.
 */
sealed trait ShowImpl[A] {
  extension (a: A) def show: String = a.toString
}

object ShowImpl:
  given defaultShowImpl[A]: ShowImpl[A] =
    new ShowImpl[A] {}

  def apply[A](formatter: A => String): ShowImpl[A] =
    new ShowImpl[A] {
      extension (a: A) override def show: String = formatter(a)
    }
