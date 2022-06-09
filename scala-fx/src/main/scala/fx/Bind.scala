package fx

extension [R, A](fa: Either[R, A]) def bind(using Errors[R]): A = fa.fold(_.shift, identity)

extension [R, A](fa: Right[R, A]) def bind: A = fa.value

extension [R, A](fa: List[Either[R, A]]) def bind(using Errors[R]): List[A] = fa.map(_.bind)

extension [A](fa: Option[A])
  def bind(using Errors[None.type]): A = fa.fold(None.shift)(identity)

extension [A](fa: Some[A]) def bind: A = fa.value
