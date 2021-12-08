package fx

import fx.*
import scala.annotation.implicitNotFound
import scala.io.StdIn.readLine

@implicitNotFound(
  "Missing capability:\n* Console"
)
trait Console:
  def consoleWrite(s: String): Unit
  def consoleReadLine(): Option[String]

extension (c: Console)
  def consoleWriteLine(s: String): Unit = 
    c.consoleWrite(s + "\n")

object StandardConsole extends Console:
  def consoleWrite(s: String): Unit = print(s)
  def consoleReadLine(): Option[String] = Some(readLine())

class FakeConsole(var input: String) extends Console:
  var output: String = ""

  def consoleWrite(s: String): Unit = output += s
  def consoleReadLine(): Option[String] =
    if input.isEmpty then
      None
    else
      input.split('\n') match
        case Array(r, rest*) =>
          input = rest.mkString("\n")
          Some(r)
        case _ =>
          None

// here I need to use summon[Console] over and over...
def program1: String * Console * Errors[String] =
  summon[Console].consoleWrite("what is your name?")
  summon[Console].consoleReadLine() match
    case None => 
      "end of input".raise
    case Some("") =>
      summon[Console].consoleWriteLine("empty name")
      program1
    case Some("me") =>
      summon[Console].consoleWriteLine("wrong name")
      "wrong name".raise
    case Some(name) =>
      name

// create some boilerplate-less methods
def consoleWrite(s: String): Unit * Console =
  summon[Console].consoleWrite(s)
def consoleWriteLine(s: String): Unit * Console =
  summon[Console].consoleWriteLine(s)
def consoleReadLine(): Option[String] * Console =
  summon[Console].consoleReadLine()

// here I need to use summon[Console] over and over...
def program2: String * Console * Errors[String] =
  consoleWrite("what is your name?")
  consoleReadLine() match
    case None => 
      "end of input".raise
    case Some("") =>
      consoleWriteLine("empty name")
      program2
    case Some("me") =>
      consoleWriteLine("wrong name")
      "wrong name".raise
    case Some(name) =>
      name