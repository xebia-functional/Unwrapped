import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec

/** Within the standard library we declare a simple Colour enumerated type and
  * then we make the suspend trait marker take a type parameter with the colour.
  *
  * The "Suspend" will indicate the potential to suspend, but the colour will
  * indicate whether it really needs to suspend or not. 
  */
sealed trait Colour
sealed trait Red extends Colour
sealed trait Green extends Red // green is stronger than red: 

trait Suspend[- L <: Colour] // suspend: we want Suspend[Red] to substitute for a Suspend[Green]

implicit def greenWash[A, B](f: A => B): (A => Suspend[Green] => B) = ???

extension [A](list: List[A]) {
  def greenMap[B](f: A => B): List[B] =
    val buf = ListBuffer.empty[B]
    @tailrec
    def go(ys: List[A]): Unit = ys match {
      case Nil => ()
      case y :: ys =>
        buf += f(y)
        go(ys)
    }
    go(list)
    buf.toList

  def redMap[B](f: A => Suspend[Red] ?=> B)(using Suspend[Red]): List[B] =
    val buf = ListBuffer.empty[B]
    @tailrec
    def go(ys: List[A]): Unit = ys match {
      case Nil => ()
      case y :: ys =>
        buf += f(y)
        go(ys)
    }
    go(list)
    buf.toList

  def mapC[B, C <: Colour](f: A => Suspend[C] ?=> B)(using Suspend[C]): List[B] = {
    val buf = ListBuffer.empty[B]
    @tailrec
    def go(ys: List[A]): Unit = ys match {
      case Nil => ()
      case y :: ys =>
        buf += f(y)
        go(ys)
    }
    go(list)
    buf.toList
  }
}

given g: Suspend[Green] = new Suspend[Green] {}
//given r: Suspend[Red] = new Suspend[Red] {}

def fili(i: Int)(using Suspend[Green]): Char = ???
def kili(i: Int)(using Suspend[Red]): Char = ???

def nums = List(1,2,3,4,5,6)

def filis = nums.mapC(fili) // inferred type argument [Green]
def kilis(using Suspend[Red]) = nums.mapC(kili) // this would type-check

def filikilis(using Suspend[Red]) =
  nums.mapC(fili) // inferred typed Suspend[Red]
//def kilifilis(using Suspend[Green]) = 
//  nums.camaleonMap(kili) // this should type-fail

def callMapManyTimes(xs: List[Int])(using Suspend[Red]): List[Char] =
  val a = xs.mapC(fili) // Green
  val b = xs.mapC(kili) // Red
  a ++ b

/** mapSQ: we define new higher-order functions out of existing higuer-order functions
  */
extension[A] (xss: List[List[A]])
  def mapSq[B, C <: Colour](f: A => Suspend[C] ?=> B)(using Suspend[C]): List[List[B]] =
    xss.mapC(_.mapC(f))

def sudoku = List( List( 3, 4, 7 ), List(1, 9, 8), List(2, 5, 6) )

def filisSQ = sudoku.mapSq(fili)
def kilisSQ(using Suspend[Red]) = sudoku.mapSq(kili)

//def polychromatic[C <: Colour, D <: Colour](
//  xs: List[Int],
//  ori: Int => Suspend[C] ?=> Char)(
//  nori: Int => Suspend[D] ?=> Char)(
//  using Suspend[C & D]
//): List[Char] =
//  xs.camaleonMap(ori) ++ xs.camaleonMap(nori)


def composeC[X, Y, Z, C <: Colour, D <: Colour](
  pre: X => Suspend[C] ?=> Y,
  post: Y => Suspend[D] ?=> Z):
    X => Suspend[C | D] ?=> Z =
  (x: X) => post(pre(x))


def dori[A <: Colour](i: Int)(using Suspend[A]): Int = i * i
def ori(i: Int)(using Suspend[Green]): Int = i * 2
def nori(i: Int)(using Suspend[Red]): Int = i + 21

def doriori(i: Int)(using Suspend[Green]): Int = composeC(dori, ori)(i)
def oridori(i: Int)(using Suspend[Green]): Int = composeC(ori, dori)(i)
def dorinori(i: Int)(using Suspend[Red]): Int = composeC(dori, nori)(i)
def noridori(i: Int)(using Suspend[Red]): Int = composeC(nori, dori)(i)

def oriori(i: Int)(using Suspend[Green]): Int = composeC(ori, ori)(i)
def noriori(i: Int)(using Suspend[Red]): Int = composeC(nori, ori)(i)
def orinori(i: Int)(using Suspend[Red]): Int = composeC(ori, nori)(i)
def norinori(i: Int)(using Suspend[Red]): Int = composeC(nori, nori)(i)

def oridorinori(i: Int)(using Suspend[Red]): Int = composeC(composeC(ori, dori), nori)(i)

/** Potentially we can also build point-free declarations.
  * 
  * But what would they compile to? 
 */
def oridorinoriPF: Int => Suspend[Red] ?=> Int = composeC(composeC(ori, dori[Green]), nori)

/**
  So, why are all of these examples relevant? It shows that colour-polymorphism fixes many of the problems of "What colour is your function"

  - Flexibility: We can declare and define higher-order functions that are applied indistinctively to both green and red functions.
  - Compositionality: We can pipe and build more higher-order functions multi-colour functions on top of existing higuer-order multi-colour functions,
  - Multi-colour: we can declare HOF that take _several_ HOF functions as a parameter, and the colours of each function can be different.
    Thus, we could have a multi-colour polymorphic function in which, depending of the arguments of the invokation, some calls are red and others are green.
 */

/**
  The valentine example https://en.wikipedia.org/wiki/Roses_Are_Red shows that, with the use of colours,
  although we are inside a "red" context, we can still have green calls.

  Note that the equivalent of this code using the `cats` combinators would look like: 
  ```
    plants.traverse { (p: Int) =>
      select(p).flatMap(b => if (b) then roses(p) else violets(p).pure[F] )
    }
  ```
 */
def valentine[C <: Colour, D <: Colour, E <: Colour](plants: List[Int])(
  select: Int => Suspend[C] ?=> Boolean,
  roses: Int => Suspend[D] ?=> Char)(
  violets: Int => Suspend[E] ?=> Char)(
  using Suspend[C | D | E]
): List[Char] =
  plants.map( (p: Int) => if (select(p)) roses(p) else violets(p))

/* In addition to parametric polymorphism, we could also start supporting type-members */
trait Dwarf {
  type A <: Colour
  def dig(i: Int)(using Suspend[A]): Char
}

class Bifur extends Dwarf {
  override type A = Green
  override def dig(i: Int)(using Suspend[A]): Char = ???
}

class Bombur extends Dwarf {
  override type A = Red
  override def dig(i: Int)(using Suspend[A]): Char = ???
}

/* AS well as GADT */
trait Dwarven[A <: Colour] {
  def dig(i: Int)(using Suspend[A]): Char
}

class Ori extends Dwarven[Green] {
  override def dig(i: Int)(using Suspend[Green]): Char = ???
}

class Nori extends Dwarven[Red] {
  override def dig(i: Int)(using Suspend[Red]): Char = ???
}

//def dori(i: Int)(using Suspend[Red]): Char =
//  val dwa = if ((i & 1) == 0) new Ori else new Nori
//  dwa.dig(i)
//
