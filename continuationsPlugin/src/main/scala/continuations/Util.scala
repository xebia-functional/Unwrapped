package continuations

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

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

  def zipWith[B, C](bs: List[B])(fun: (A, B) => C): List[C] =
    val buf = ListBuffer.empty[C]
    @tailrec
    def go(xs: List[A], ys: List[B]): Unit =
      (xs, ys) match {
        case ((xh :: xt), (yh :: yt)) =>
          buf += fun(xh, yh)
          go(xt, yt)
        case _ =>
      }
    go(list, bs)
    buf.toList

extension [A](mat: List[List[A]])
  def zipWith2[B, C](bss: List[List[B]])(fun: (A, B) => C): List[List[C]] =
    mat.zipWith(bss)(_.zipWith(_)(fun))

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
