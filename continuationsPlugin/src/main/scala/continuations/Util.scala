package continuations

import scala.annotation.tailrec

extension [A](list: List[A])
  def collectFirstOption[B](reap: A => Option[B]): Option[B] =
    @tailrec
    def go(xs: List[A]): Option[B] =
      xs match {
        case Nil => None
        case y :: ys =>
          reap(y) match {
            case None => go(ys)
            case Some(b) => Some(b)
          }
      }
    go(list)

extension [A](mat: List[List[A]])
  def exists2(pred: A => Boolean): Boolean =
    mat.exists(_.exists(pred))

  def forall2(pred: A => Boolean): Boolean =
    mat.forall(_.forall(pred))

  def find2[B](pred: A => Boolean): Option[A] =
    collectFirstOption2[A](a => if (pred(a)) Some(a) else None)

  def collectFirstOption2[B](reap: A => Option[B]): Option[B] =
    @tailrec
    def go(xxs: List[List[A]]): Option[B] =
      xxs match {
        case Nil => None
        case ys :: yss =>
          ys.collectFirstOption(reap) match {
            case Some(b) => Some(b)
            case None => go(yss)
          }
      }
    go(mat)
