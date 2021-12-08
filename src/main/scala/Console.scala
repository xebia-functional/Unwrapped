package fx

import fx.*
import scala.annotation.implicitNotFound
import scala.io.StdIn.readLine

@implicitNotFound(
  "Missing capability:\n* Console"
)
trait Console:

  def read(): String | Null

  extension (s: String)
    def write(): Unit
    def writeLine(): Unit

object Console:
  given default: Console = StandardConsole

/**
 * If your method is not an extension then it needs side syntax
 * like these or users would need to summon.
 **/
def read(): (String | Null) * Console =
  summon[Console].read()

object StandardConsole extends Console:

  def read(): String | Null = readLine()

  extension (s: String)
    def write(): Unit = print(s)
    def writeLine(): Unit = println(s)

class FakeConsole(var input: String) extends Console:
  var output: String = ""

  def read(): String | Null =
    if input.isEmpty then null
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

def program1: String * Console * Errors[String] =
  "what is your name?".write()
  read() match
    case null =>
      "end of input".raise
    case "" =>
      "empty name".writeLine()
      program1
    case "me" =>
      "wrong name".writeLine()
      "wrong name".raise
    case name =>
      name

def program2: String * Console * Errors[String] =
  "what is your name?".write()
  read() match
    case null =>
      "end of input".raise
    case "" =>
      "empty name".writeLine()
      program2
    case "me" =>
      "wrong name".writeLine()
      "wrong name".raise
    case name =>
      name

@main def consoleStandard() =
  import fx.runtime

  val value: String =
    run(program2)

@main def consoleFake() =
  import fx.runtime
  given Console = FakeConsole("Fake Input!")
  val value: String =
    run(program2)
  println(value)
