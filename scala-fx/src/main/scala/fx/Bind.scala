package fx

extension [R, A](fa: Either[R, A]) def bind(using Raise[R]): A = fa.fold(_.raise, identity)

extension [R, A](fa: Right[R, A]) def bind: A = fa.value

extension [R, A](fa: List[Either[R, A]]) def bind(using Raise[R]): List[A] = fa.map(_.bind)

extension [A](fa: Option[A]) def bind(using Raise[None.type]): A = fa.fold(None.raise)(identity)

extension [A](fa: Some[A]) def bind: A = fa.value
