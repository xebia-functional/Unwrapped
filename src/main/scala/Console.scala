package fx

import fx.*
import scala.annotation.implicitNotFound
import scala.io.StdIn.readLine
import scala.annotation.tailrec

class EndOfLine extends RuntimeException("reached end of line")

@implicitNotFound(
  "Missing capability:\n* Console"
)
trait Console:

  def read(): String * Throws[EndOfLine]

  extension (s: String)
    def write(): Unit
    def writeLine(): Unit

object Console:
  given default: Console = StandardConsole

/** If your method is not an extension then it needs side syntax like these or
  * users would need to summon.
  */
def read(): String * Console * Throws[EndOfLine] =
  summon[Console].read()

object StandardConsole extends Console:
  def read(): String * Throws[EndOfLine] =
    val r = readLine()
    if (r != null) r
    else throw EndOfLine()

  extension (s: String)
    def write(): Unit = print(s)
    def writeLine(): Unit = println(s)

class FakeConsole(var input: String) extends Console:
  var output: String = ""

  def read(): String * Throws[EndOfLine] =
    if input.isEmpty then throw EndOfLine()
    else
      input.split('\n') match
        case Array(r, rest*) =>
          input = rest.mkString("\n")
          r
        case _ =>
          null

  extension (s: String)
    def write(): Unit = output += s
    def writeLine(): Unit = output += (s + "\n")

@tailrec
def program: String * Console * Errors[String] * Throws[EndOfLine] =
  "what is your name?".writeLine()
  read() match
    case "" =>
      "empty name".writeLine()
      program
    case "me" =>
      "wrong name!".writeLine()
      "wrong name".raise
    case name =>
      s"hello $name"

@main def consoleStandard() =
  import fx.runtime
  import fx.unsafe.unsafeExceptions

  val value: String =
    run(program)

@main def consoleFake() =
  import fx.runtime
  import fx.unsafe.unsafeExceptions
  given Console = FakeConsole("")
  val value: String =
    try 
      run(program)
    catch 
      case eol: EndOfLine => "Reached end"
    
  println(value)
