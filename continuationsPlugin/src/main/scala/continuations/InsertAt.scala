package continuations

extension [A](l: List[A])
  def insertAt(index: Int, item: A): List[A] =
    val (left, right) = l.splitAt(index)
    left ::: (item :: right)
