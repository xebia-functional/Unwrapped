package continuations

import dotty.tools.dotc.ast.tpd.Tree
import dotty.tools.dotc.Compiler
import dotty.tools.dotc.core.Comments.ContextDoc
import dotty.tools.dotc.core.Comments.ContextDocstrings
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ContextBase
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Phases.Phase
import munit.FunSuite

import scala.concurrent.Promise
import scala.util.Properties
import scala.util.Try

trait CompilerFixtures { self: FunSuite =>

  val compileSourceIdentifier =
    """-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.""".r

  protected def compilerWithChecker(phase: String)(assertion: (Tree, Context) => Unit) =
    new Compiler {
      override def phases = {
        val allPhases = super.phases
        val targetPhase = allPhases.flatten.find(p => p.phaseName == phase).get
        val groupsBefore = allPhases.takeWhile(x => !x.contains(targetPhase))
        val lastGroup =
          allPhases.find(x => x.contains(targetPhase)).get.takeWhile(x => !(x eq targetPhase))
        val checker = new Phase {
          def phaseName = "assertionChecker"

          override def run(using ctx: Context): Unit =
            assertion(ctx.compilationUnit.tpdTree, ctx)
        }
        val lastGroupAppended = List(lastGroup ::: targetPhase :: Nil)

        groupsBefore ::: lastGroupAppended ::: List(List(checker))
      }
    }

  def checkCompile(checkAfterPhase: String, source: String)(assertion: (Tree, Context) => Unit)(
      using Context): Context = {
    val c = compilerWithChecker(checkAfterPhase)(assertion)
    val run = c.newRun
    run.compileFromStrings(List(source))
    run.runContext
  }

  def checkContinuations(source: String)(assertion: (Tree, Context) => Unit)(
      using Context): Context = {
    checkCompile("pickleQuotes", source)(assertion)
  }

  def continuationsCompilerSnapshot(source: String)(using Context): (String, String) =
    val p = Promise[(String, String)]
    checkContinuations(source)((t, _) =>
      p.complete(
        Try(
          (
            compileSourceIdentifier.replaceAllIn(t.toString, ""),
            compileSourceIdentifier.replaceAllIn(t.show, "")))))
    p.future.value.get.get

  val compilerContextWithContinuationsPlugin = FunFixture(
    setup = _ => {
      val base = new ContextBase {}
      val compilerPlugin = Properties.propOrEmpty("scala-compiler-plugin")
      val compilerClasspath = Properties.propOrEmpty(
        "scala-compiler-classpath") ++ s":${Properties.propOrEmpty("scala-compiler-plugin")}"
      val context = base.initialCtx.fresh
      context.setSetting(context.settings.color, "never")
      context.setSetting(context.settings.encoding, "UTF8")
      context.setSetting(context.settings.language, List("experimental.erasedDefinitions"))
      context.setSetting(context.settings.noindent, true)
      context.setSetting(context.settings.XprintDiffDel, true)
      context.setSetting(context.settings.pageWidth, 149)
      if (compilerPlugin.nonEmpty) {
        context.setSetting(context.settings.classpath, compilerClasspath)
      }

      context.setSetting(
        context.settings.plugin,
        List(Properties.propOrEmpty("scala-compiler-plugin")))
      context.setProperty(ContextDoc, new ContextDocstrings)
      base.initialize()(using context)
      context
    },
    teardown = _ => ()
  )
}
