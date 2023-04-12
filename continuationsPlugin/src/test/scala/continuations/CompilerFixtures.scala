// format: off
/**
 * Dotty (https://dotty.epfl.ch) Copyright 2012-2020
 *
 * EPFL Copyright 2012-2020 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"):
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * The dotty compiler frontend has been developed since November 2012 by Martin Odersky. It is
 * expected and hoped for that the list of contributors to the codebase will grow quickly. Dotty
 * draws inspiration and code from the original Scala compiler "nsc", which is developed at
 * scala/scala [1].
 *
 * The majority of the dotty codebase is new code, with the exception of the components
 * mentioned below. We have for each component tried to come up with a list of the original
 * authors in the scala/scala [1] codebase. Apologies if some major authors were omitted by
 * oversight.
 *
 * * dotty.tools.dotc.ast: The syntax tree handling is mostly new, but some elements, such as
 * the idea of tree copiers and the `TreeInfo` module, were adopted from scala/scala [1]. The
 * original authors of these parts include Martin Odersky, Paul Phillips, Adriaan Moors, and
 * Matthias Zenger.
 *
 * * dotty.tools.dotc.classpath: The classpath handling is taken mostly as is from scala/scala
 * [1]. The original authors were Grzegorz Kossakowski, Michał Pociecha, Lukas Rytz, Jason Zaugg
 * and others.
 *
 * * dotty.tools.dotc.config: The configuration components were adapted and extended from
 * scala/scala [1]. The original sources were authored by Paul Phillips with contributions from
 * Martin Odersky, Miguel Garcia and others.
 *
 * * dotty.tools.dotc.core: The core data structures and operations are mostly new. Some parts
 * (e.g. those dealing with names) were adapted from scala/scala [1]. These were originally
 * authored by Martin Odersky, Adriaan Moors, Jason Zaugg, Paul Phillips, Eugene Burmako and
 * others.
 *
 * * dotty.tools.dotc.core.pickling: The classfile readers were adapted from the current Scala
 * compiler. Original authors were Martin Odersky, Iulian Dragos, Matthias Zenger and others.
 *
 * * dotty.tools.dotc.parsing: The lexical and syntactic analysis components were adapted from
 * the current Scala compiler. They were originally authored by Martin Odersky, Burak Emir, Paul
 * Phillips, Lex Spoon, Sean McDirmid and others.
 *
 * * dotty.tools.dotc.profile: The per-phase profiling support is taken mostly as is from
 * scala/scala. The original author was Mike Skells.
 *
 * * dotty.tools.dotc.reporting: Adapted from scala/scala [1] with some heavy modifications.
 * They were originally authored by Matthias Zenger, Martin Odersky, and others.
 *
 * * dotty.tools.dotc.typer: This is new code except for some minor components (e.g. the
 * ConstantFolder). It uses however many solution details that have been developed over time by
 * many people, including Jason Zaugg, Adriaan Moors, Lukas Rytz, Paul Phillips, Grzegorz
 * Kossakowski, and others.
 *
 * * dotty.tools.dotc.util: The utilities package is a mix of new and adapted components. The
 * files in scala/scala [1] were originally authored by many people, including Paul Phillips,
 * Martin Odersky, Sean McDirmid, and others.
 *
 * * dotty.tools.io: The I/O support library was adapted from current Scala compiler. Original
 * authors were Paul Phillips and others.
 *
 * * dotty.test.DottyBytecodeTest: Is an adaptation of the bytecode testing from scala/scala
 * [1]. It has been reworked to fit the needs of dotty. Original authors include: Adrian Moors,
 * Lukas Rytz, Grzegorz Kossakowski, Paul Phillips.
 *
 * * dotty.tools.dotc.sbt and everything in sbt-bridge: The sbt compiler phases are based on [2]
 * which attempts to integrate the sbt phases into scalac and is itself based on the compiler
 * bridge in sbt 0.13 [3], but has been heavily adapted and refactored. Original authors were
 * Mark Harrah, Grzegorz Kossakowski, Martin Duhemm, Adriaan Moors and others.
 *
 * * dotty.tools.dotc.plugins: Adapted from scala/scala [1] with some modifications. They were
 * originally authored by Lex Spoon, Som Snytt, Adriaan Moors, Paul Phillips and others.
 *
 * * dotty.tools.scaladoc: The Scaladoc documentation utility ships some third-party JavaScript
 * and CSS libraries which are located under scaladoc/resources/dotty_res/styles/,
 * scaladoc/resources/dotty_res/scripts/, docs/css/ and docs/js/. Please refer to the license
 * header of the concerned files for details.
 *
 * * dotty.tools.dotc.coverage: Coverage instrumentation utilities have been adapted from the
 * scoverage plugin for scala 2 [5], which is under the Apache 2.0 license.
 *
 * * The Dotty codebase contains parts which are derived from the ScalaPB protobuf library [4],
 * which is under the Apache 2.0 license.
 *
 * [1] https://github.com/scala/scala
 * [2] https://github.com/adriaanm/scala/tree/sbt-api-consolidate/src/compiler/scala/tools/sbt
 * [3] https://github.com/sbt/sbt/tree/0.13/compile/interface/src/main/scala/xsbt
 * [4] https://github.com/lampepfl/dotty/pull/5783/files
 * [5] https://github.com/scoverage/scalac-scoverage-plugin
 * 
 * The below fixtures and methods were adapted from the dotty tools DottyTest class [6].
 * https://github.com/lampepfl/dotty/blob/main/compiler/test/dotty/tools/DottyTest.scala
 *   - Accessed on 2022/10/08T22:20:00.000Z-5:00 [6]
 */
// format: on

package continuations

import dotty.tools.dotc.Compiler
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Comments.ContextDoc
import dotty.tools.dotc.core.Comments.ContextDocstrings
import dotty.tools.dotc.core.Contexts.{ctx, Context, ContextBase}
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.Phases.Phase
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{ContextualMethodType, MethodType, Type, TypeRef}
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Flags.EmptyFlags
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Types
import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.core.Definitions
import dotty.tools.dotc.core.StdNames.nme
import munit.FunSuite
import scala.util.Properties
import dotty.tools.dotc.core.Contexts.FreshContext
import munit.Location
import munit.internal.console.StackTraces
import munit.internal.difflib.Diffs
import munit.internal.difflib.ComparisonFailExceptionHandler
import dotty.tools.dotc.core.TypeComparer

trait CompilerFixtures { self: FunSuite =>

  def removeLineTrailingSpaces(s: String): String =
    s.lines.map(_.stripTrailing).reduce(_ ++ "\n" ++ _).get

  def cleanCompilerOutput(tree: Tree)(using Context): String = removeLineTrailingSpaces(
    compileSourceIdentifier.replaceAllIn(tree.show, ""))

  private def usingSuspend(owner: Symbol)(using c: Context): Symbol =
    Symbols.newSymbol(
      owner,
      nme.x_1,
      Flags.union(Flags.GivenOrImplicit, Flags.Param),
      suspendType
    )

  private def suspendIntMethod(owner: Symbol)(using c: Context): Symbol =
    Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType)

  val compileSourceIdentifier =
    """-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.""".r

  protected def compiler(phase: String) = new Compiler {
    override def phases = {
      val allPhases = super.phases
      val targetPhase = allPhases.flatten.find(p => p.phaseName == phase).get
      val groupsBefore = allPhases.takeWhile(x => !x.contains(targetPhase))
      val lastGroup =
        allPhases.find(x => x.contains(targetPhase)).get.takeWhile(x => !(x eq targetPhase))
      val lastGroupAppended = List(lastGroup ::: targetPhase :: Nil)

      groupsBefore ::: lastGroupAppended
    }
  }

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

  lazy val compilerContextWithContinuationsPlugin: FunFixture[FreshContext] = FunFixture(
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
    teardown = ctx => ()
  )

  val compilerContext = FunFixture(
    setup = _ => {
      val base = new ContextBase {}
      val context = base.initialCtx.fresh
      context.setSetting(context.settings.encoding, "UTF8")
      context.setSetting(context.settings.language, List("experimental.erasedDefinitions"))
      val compilerClasspath = Properties.propOrEmpty(
        "scala-compiler-classpath") ++ s":${Properties.propOrEmpty("scala-compiler-plugin")}"
      context.setSetting(context.settings.classpath, compilerClasspath)
      context.setProperty(ContextDoc, new ContextDocstrings)
      base.initialize()(using context)
      context
    },
    teardown = ctx => {}
  )

  val suspendContextFunction = FunFixture[Context ?=> Type](
    setup = _ => ctxFunctionTpe,
    teardown = _ => ()
  )

  val suspendContextFunctionReturningSuspend = FunFixture[Context ?=> Type](
    setup = _ => {
      val c = summon[Context]
      c.definitions
        .asContextFunctionType(
          c.definitions.FunctionOf(List(suspend.termRef), suspend.termRef, true, false))
    },
    teardown = _ => ()
  )

  val nonSuspendContextFunctionReturingSuspend = FunFixture[Context ?=> Type](
    setup = _ => {
      val c = summon[Context]
      c.definitions
        .asContextFunctionType(
          c.definitions.FunctionOf(List(c.definitions.IntType), suspend.termRef, true, false))
    },
    teardown = _ => ()
  )

  val nonSuspendContextFunction = FunFixture[Context ?=> Type](
    setup = _ => nonSuspendingContextFunctionType,
    teardown = _ => ()
  )

  val zeroArityContextFunctionDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      DefDef(
        Symbols
          .newSymbol(
            owner,
            mySuspendName,
            EmptyFlags,
            ctxFunctionTpe
          )
          .asTerm,
        List.empty,
        ctxFunctionTpe,
        one
      )
    },
    teardown = _ => ()
  )

  val suspendingContextFunctionValDef = FunFixture[Context ?=> ValDef](
    setup = _ => {
      ValDef(
        Symbols
          .newSymbol(
            owner,
            mySuspendName,
            EmptyFlags,
            ctxFunctionTpe
          )
          .asTerm,
        one
      )
    },
    teardown = _ => ()
  )

  val suspendContextualMethod: Context ?=> DefDef = DefDef(
    newAnonFun(owner, ContextualMethodType(List(suspendType), intType)),
    List(List(usingSuspend)),
    intType,
    inlinedCallToContinuationsSuspendOfIntNotInLastRow
  )

  val suspendContextualMethodDefDef =
    FunFixture[Context ?=> DefDef](
      setup = _ => suspendContextualMethod,
      teardown = _ => ()
    )

  val notSuspendContextualMethodDefDef =
    FunFixture[Context ?=> DefDef](
      setup = _ =>
        DefDef(
          newAnonFun(owner, ContextualMethodType(List(defn.StringType), intType)),
          List(List(usingSuspend)),
          intType,
          inlinedCallToContinuationsSuspendOfIntNotInLastRow
        ),
      teardown = _ => ()
    )

  val zeroArityContextFunctionWithSuspensionNotInLastRowDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      DefDef(
        newSymbol(
          owner,
          mySuspendName,
          EmptyFlags,
          ctxFunctionTpe
        ).asTerm,
        List.empty,
        ctxFunctionTpe,
        Block(
          List(
            suspendContextualMethod
          ),
          EmptyTree
        )
      )
    },
    teardown = _ => ()
  )

  val nonSuspendingContextFunctionValDef = FunFixture[Context ?=> ValDef](
    setup = _ => {
      ValDef(
        Symbols
          .newSymbol(
            owner,
            mySuspendName,
            EmptyFlags,
            nonSuspendingContextFunctionType
          )
          .asTerm,
        one
      )
    },
    teardown = _ => ()
  )

  val zeroArityNonSuspendNonSuspendingDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      DefDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType).asTerm,
        List(
          List(),
          List(Symbols.newSymbol(owner, Names.termName("s"), Flags.Param, suspendType))),
        intType,
        one
      )
    },
    teardown = _ => ()
  )

  val shift = FunFixture[Context ?=> Inlined](
    setup = _ => inlinedCallToContinuationsSuspendOfInt,
    teardown = _ => ()
  )

  val zeroAritySuspendSuspendingDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      val c = summon[Context]
      DefDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        inlinedCallToContinuationsSuspendOfInt
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendSuspendingNotInLastRowDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      DefDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        inlinedCallToContinuationsSuspendOfIntNotInLastRow
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendSuspendingNotInLastRowIfDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      DefDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        If(
          Literal(Constant(true)),
          Block(List(inlinedCallToContinuationsSuspendOfInt), Literal(Constant(9))),
          Literal(Constant(10)))
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendSuspendingInLastRowIfDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      DefDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        If(
          Literal(Constant(true)),
          inlinedCallToContinuationsSuspendOfInt,
          Literal(Constant(10)))
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendNonSuspendingDefDef = FunFixture[Context ?=> DefDef](
    setup = _ => {
      val c = summon[Context]
      val rhs =
        one
      DefDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        rhs
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendSuspendingValDef = FunFixture[Context ?=> ValDef](
    setup = _ => {
      ValDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, ctxFunctionTpe).asTerm,
        inlinedCallToContinuationsSuspendOfInt
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendNonSuspendingValDef = FunFixture[Context ?=> ValDef](
    setup = _ => {
      ValDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, ctxFunctionTpe).asTerm,
        Literal(Constant(10))
      )
    },
    teardown = _ => ()
  )

  val zeroAritySuspendSuspendingNotInLastRowValDef = FunFixture[Context ?=> ValDef](
    setup = _ => {
      ValDef(
        Symbols.newSymbol(owner, mySuspendName, EmptyFlags, ctxFunctionTpe).asTerm,
        inlinedCallToContinuationsSuspendOfIntNotInLastRow
      )
    },
    teardown = _ => ()
  )

  val rightOneApply: FunFixture[Context ?=> Apply] =
    FunFixture(setup = _ => rightOne, teardown = _ => ())

  val oneTree: FunFixture[Context ?=> Literal] =
    FunFixture(setup = _ => one, teardown = _ => ())

  val suspendingSingleArityWithDependentNonSuspendingAndNonDependentCalculation =
    FunFixture[Context ?=> DefDef](
      setup = _ => {
        val c = summon[Context]
        val mySuspend = suspendIntMethod(c.owner)
        val x = Symbols.newSymbol(mySuspend, Names.termName("x"), Flags.Param, intType)
        val usingSuspendArg = usingSuspend(mySuspend)
        val y =
          Symbols.newSymbol(mySuspend, Names.termName("y"), Flags.EmptyFlags, intType)
        val continuationName = Names.termName("continuation")
        val closureMethodType = MethodType.apply(
          List(
            continuationName
          ),
          List(
            continuationVal.info
          ),
          c.definitions.UnitType
        )
        DefDef(
          mySuspend.asTerm,
          List(List(x), List(usingSuspendArg)),
          intType,
          Block(
            List(
              ValDef(
                y.asTerm,
                ref(usingSuspendArg)
                  .select(Names.termName(shiftName))
                  .appliedTo(ref(usingSuspendArg))
                  .appliedToType(intType)
                  .appliedTo(
                    Lambda(
                      closureMethodType,
                      (tss: List[Tree]) =>
                        Block(
                          List(
                          ),
                          ref(tss.head.symbol)
                            .select(Names.termName(resumeName))
                            .appliedTo(
                              ref(right)
                                .select(Names.termName("apply"))
                                .appliedToTypes(
                                  List(c.definitions.ThrowableType, intType)
                                )
                                .appliedTo(
                                  ref(x).select(defn.Int_+).appliedTo(one)
                                )
                            )
                        )
                    )
                  )
              ),
              inlinedCallToContinuationsSuspendOfInt
            ),
            ref(x).select(defn.Int_+).appliedTo(ref(y))
          )
        )
      },
      teardown = _ => ()
    )

  val inlinedSuspendingSingleArityWithDependentNonSuspendingCalculation =
    FunFixture[Context ?=> DefDef](
      setup = _ => {
        val c = summon[Context]
        val mySuspend = suspendIntMethod(c.owner)
        val x = Symbols.newSymbol(mySuspend, Names.termName("x"), Flags.Param, intType)
        val usingSuspendArg = usingSuspend(mySuspend)
        val y =
          Symbols.newSymbol(mySuspend, Names.termName("y"), Flags.EmptyFlags, intType)
        val continuationName = Names.termName("continuation")
        val closureMethodType = MethodType.apply(
          List(
            continuationName
          ),
          List(
            continuationVal.info
          ),
          c.definitions.UnitType
        )
        DefDef(
          mySuspend.asTerm,
          List(List(x), List(usingSuspendArg)),
          intType,
          Block(
            List(
              ValDef(
                y.asTerm,
                Inlined(
                  ref(usingSuspendArg)
                    .select(Names.termName(shiftName))
                    .appliedTo(ref(usingSuspendArg))
                    .appliedToType(intType)
                    .appliedTo(
                      Lambda(
                        closureMethodType,
                        (tss: List[Tree]) =>
                          Block(
                            List(
                            ),
                            ref(tss.head.symbol)
                              .select(Names.termName("resume"))
                              .appliedTo(
                                ref(right)
                                  .select(Names.termName("apply"))
                                  .appliedToTypes(
                                    List(c.definitions.ThrowableType, intType)
                                  )
                                  .appliedTo(
                                    ref(x).select(defn.Int_+).appliedTo(one)
                                  )
                              )
                          )
                      )
                    ),
                  List(),
                  Typed(
                    Ident(requiredMethod("scala.Predef.???").typeRef),
                    TypeTree(c.definitions.IntType)
                  )
                )
              )
            ),
            ref(x).select(defn.Int_+).appliedTo(ref(y))
          )
        )
      },
      teardown = _ => ()
    )

  val methodCallWithSuspend =
    FunFixture[Context ?=> Block](
      setup = _ => {
        val methodSymbol =
          Symbols.newSymbol(
            owner,
            mySuspendName,
            Flags.Method,
            MethodType(
              List.empty,
              List.empty,
              ContextualMethodType(List(nme.x_1), List(suspendType), intType)
            )
          )
        Block(
          List(
            DefDef(
              methodSymbol.asTerm,
              List(List(), List(usingSuspend)),
              intType,
              Literal(Constant(10))
            )),
          ref(methodSymbol).appliedToNone.appliedTo(ref(usingSuspend))
        )
      },
      teardown = _ => ()
    )

  val methodCallWithoutSuspend =
    FunFixture[Context ?=> Block](
      setup = _ => {
        val methodSymbol =
          Symbols.newSymbol(
            owner,
            mySuspendName,
            Flags.Method,
            MethodType(
              List.empty,
              List.empty,
              ContextualMethodType(List(nme.x_1), List(continuation.typeRef), intType)
            )
          )
        val usingContinuation =
          Symbols.newSymbol(
            owner,
            Names.termName("x$1"),
            Flags.union(Flags.GivenOrImplicit, Flags.Param),
            continuation.typeRef
          )
        Block(
          List(
            DefDef(
              methodSymbol.asTerm,
              List(List(), List(usingContinuation)),
              intType,
              Literal(Constant(10))
            )),
          ref(methodSymbol).appliedToNone)
      },
      teardown = _ => ()
    )

  val continuationsContextAndInlinedSuspendingTree =
    FunFixture.map2(compilerContextWithContinuationsPlugin, shift)

  val continuationsContextAndOneTree =
    FunFixture.map2(compilerContextWithContinuationsPlugin, oneTree)

  val continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      zeroArityNonSuspendNonSuspendingDefDef)

  val continuationsContextAndZeroAritySuspendSuspendingDefDef =
    FunFixture.map2(compilerContextWithContinuationsPlugin, zeroAritySuspendSuspendingDefDef)

  val continuationsContextAndZeroAritySuspendSuspendingNotInLastRowDefDef =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      zeroAritySuspendSuspendingNotInLastRowDefDef)

  val continuationsContextAndZeroAritySuspendSuspendingNotInLastRowIfDefDef =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      zeroAritySuspendSuspendingNotInLastRowIfDefDef)

  val continuationsContextAndZeroAritySuspendSuspendingInLastRowIfDefDef =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      zeroAritySuspendSuspendingInLastRowIfDefDef)

  val continuationsContextAndZeroAritySuspendNonSuspendingDefDef =
    FunFixture.map2(compilerContextWithContinuationsPlugin, zeroAritySuspendNonSuspendingDefDef)

  val continuationsContextAndSuspendContextFunction =
    FunFixture.map2(compilerContextWithContinuationsPlugin, suspendContextFunction)

  val continuationsContextAndNonSuspendContextFunctionReturingSuspend = FunFixture.map2(
    compilerContextWithContinuationsPlugin,
    nonSuspendContextFunctionReturingSuspend)

  val continuationsContextAndSuspendContextFunctionReturningSuspend = FunFixture.map2(
    compilerContextWithContinuationsPlugin,
    suspendContextFunctionReturningSuspend)

  val continuationsContextAndNonSuspendContextFunction = FunFixture.map2(
    compilerContextWithContinuationsPlugin,
    nonSuspendContextFunction
  )

  val continuationsContextAndZeroArityContextFunctionDefDef = FunFixture.map2(
    compilerContextWithContinuationsPlugin,
    zeroArityContextFunctionDefDef
  )

  val continuationsContextAndZeroArityContextFunctionWithSuspensionNotInLastRowDefDef =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      zeroArityContextFunctionWithSuspensionNotInLastRowDefDef
    )

  val continuationsContextAndSuspendingContextFunctionValDef = FunFixture.map2(
    compilerContextWithContinuationsPlugin,
    suspendingContextFunctionValDef
  )

  val continuationsContextAndNonSuspendingContextFunctionValDef = FunFixture.map2(
    compilerContextWithContinuationsPlugin,
    nonSuspendingContextFunctionValDef
  )

  val continuationsContextAndSuspendContextualMethodDefDef =
    FunFixture.map2(compilerContextWithContinuationsPlugin, suspendContextualMethodDefDef)

  val continuationsContextAndNotSuspendContextualMethodDefDef =
    FunFixture.map2(compilerContextWithContinuationsPlugin, notSuspendContextualMethodDefDef)

  val continutationsContextAndSuspendingSingleArityWithDependentNonSuspendingAndNonDependentCalculation =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      suspendingSingleArityWithDependentNonSuspendingAndNonDependentCalculation)

  val continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCalculation =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      inlinedSuspendingSingleArityWithDependentNonSuspendingCalculation)

  val continutationsContextAndMethodCallWithSuspend =
    FunFixture.map2(compilerContextWithContinuationsPlugin, methodCallWithSuspend)

  val continutationsContextAndMethodCallWithoutSuspend =
    FunFixture.map2(compilerContextWithContinuationsPlugin, methodCallWithoutSuspend)

  val continuationsContextAndZeroAritySuspendSuspendingValDef =
    FunFixture.map2(compilerContextWithContinuationsPlugin, zeroAritySuspendSuspendingValDef)

  val continuationsContextAndZeroAritySuspendNonSuspendingValDef =
    FunFixture.map2(compilerContextWithContinuationsPlugin, zeroAritySuspendNonSuspendingValDef)

  val continuationsContextAndZeroAritySuspendSuspendingNotInLastRowValDef =
    FunFixture.map2(
      compilerContextWithContinuationsPlugin,
      zeroAritySuspendSuspendingNotInLastRowValDef)

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

  def checkTypes(source: String, typeStrings: String*)(
      assertion: (List[Type], Context) => Unit)(using Context): Unit =
    checkTypes(source, List(typeStrings.toList)) { (tpess, ctx) =>
      (tpess: @unchecked) match {
        case List(tpes) => assertion(tpes, ctx)
      }
    }

  /**
   * Asserts that two types are equal using `=:=` equality.
   *
   * The "expected" value (second argument) must be the same type as the "obtained" value (first argument). For example:
   * {{{
   *   assertEquals(IntType, IntType) // OK
   *   assertEquals(StringType, requiredClass(someClassFullName).typeRef) // Fails with a diff of the types' show arguments
   * }}}
   * Note that the diff does not normalize any type param info
   * names (for now), so only the actual types and positions matter,
   * as they do in `=:=`.
   * E.g. in 
   * {{{
   *   ==> X continuations.types.ExampleTypeMapSuite.it should remove using Suspend from a method's type and insert a continuation of the final result type as a new type list at the index prior to the Suspend type.  0.719s munit.ComparisonFailException: /home/jackcviers/Documents/development/TBD/continuationsPlugin/src/test/scala/continuations/types/ExampleTypeMapSuite.scala:61
   *   60:        ctx.definitions.IntType)
   *   61:      assertTypeEquals(actualType, expectedType)
   *   62:  }
   *   values are not the same
   *   => Obtained
   *   (x$0: Int, x$1: Int)(x$0: Int): Int
   *   => Diff (- obtained, + expected)
   *   -(x$0: Int, x$1: Int)(x$0: Int): Int
   *   +(a: Int, b: Int)(completion: continuations.Continuation[Int])(c: Int): Int
   * }}} the `x$0`, `x$1`, and second `x$0` in obtained are equivalent
   * to `a`, `b`, and `c` in actual, because they are in the correct
   * position with the correct type. However, `completion:
   * continuations.Continuation[Int]` is missing in obtained, making
   * the types non-equivalent. The diff is intended to assist you in
   * fixing the non-equivalency.
   */
  def assertTypeEquals[A <: Type, B <: Type](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same"
  )(using Location, Context): Unit = {
    def munitComparisonHandler(
        actualObtained: Any,
        actualExpected: Any
    ): ComparisonFailExceptionHandler =
      new ComparisonFailExceptionHandler {
        override def handle(
            message: String,
            unusedObtained: String,
            unusedExpected: String,
            loc: Location
        ): Nothing = failComparison(message, actualObtained, actualExpected)(loc)
      }
    StackTraces.dropInside {
      if (!(TypeComparer.isSameType(obtained, expected))) {
        Diffs.assertNoDiff(
          munitPrint(obtained.show),
          munitPrint(expected.show),
          munitComparisonHandler(obtained.show, expected.show),
          munitPrint(clue),
          printObtainedAsStripMargin = false
        )
      }
    }
  }
  /**
   * Asserts that two types are not equal using `=:=` equality.
   *
   * The "expected" value (second argument) must not be the same type as the "obtained" value (first argument). For example:
   * {{{
   *   assertEquals(IntType, IntType) // Fails showing the types of obtained and expected were the same type.
   *   assertEquals(StringType, requiredClass(someClassFullName).typeRef) // Ok
   * }}}
   * Note that the show does not normalize any type param info
   * names (for now), so only the actual types and positions matter,
   * as they do in `=:=`. That is two method types of the same arity with the types in the same positions are equal.
   */
  def assertNotTypeEquals[A <: Type, B <: Type](
      obtained: A,
      expected: B
  )(using Location, Context): Unit = {
    StackTraces.dropInside {
      if (!TypeComparer.isSameType(obtained, expected)) {
        failComparison(
          s"expected different types: ${expected.show} =:= ${obtained.show}",
          obtained,
          expected
        )
      }
    }
  }

  def checkTypes(source: String, typeStringss: List[List[String]])(
      assertion: (List[List[Type]], Context) => Unit)(using Context): Unit = {
    val dummyName = "x_x_x"
    val vals = typeStringss
      .flatten
      .zipWithIndex
      .map { case (s, x) => s"val ${dummyName}$x: $s = ???" }
      .mkString("\n")
    val gatheredSource = s"${source}\nobject A$dummyName {$vals}"
    checkCompile("typer", gatheredSource) { (tree, context) =>
      given Context = context
      val findValDef: (List[ValDef], Tree) => List[ValDef] =
        (acc, tree) => {
          tree match {
            case t: ValDef if t.name.startsWith(dummyName) => t :: acc
            case _ => acc
          }
        }
      val d = new DeepFolder[List[ValDef]](findValDef).foldOver(Nil, tree)
      val tpes = d.map(_.tpe.widen).reverse
      val tpess = typeStringss
        .foldLeft[(List[Type], List[List[Type]])]((tpes, Nil)) {
          case ((rest, result), typeStrings) =>
            val (prefix, suffix) = rest.splitAt(typeStrings.length)
            (suffix, prefix :: result)
        }
        ._2
        .reverse
      assertion(tpess, context)
    }
  }

  def methType(names: String*)(paramTypes: Type*)(using Context)(
      resultType: Type = defn.UnitType) =
    MethodType(names.toList map (_.toTermName), paramTypes.toList, resultType)

  private lazy val nonSuspendingContextFunctionType: Context ?=> Type =
    c.definitions
      .asContextFunctionType(c.definitions.FunctionOf(List(intType), intType, true, false))

  private lazy val owner: Context ?=> Symbol =
    c.owner

  private lazy val ctxFunctionTpe: Context ?=> Type =
    c.definitions
      .asContextFunctionType(
        c.definitions.FunctionOf(List(suspend.termRef), intType, true, false))

  private lazy val mySuspendName: Context ?=> Names.SimpleName =
    Names.termName("mySuspend")

  private lazy val one: Context ?=> Literal =
    Literal(Constant(1))

  private lazy val suspendType: Context ?=> Type =
    suspend.typeRef

  private lazy val continuation: Context ?=> Symbol =
    Symbols.requiredClass("continuations.Continuation")

  private def c(using Context): Context = summon[Context]

  private lazy val anonFunc: Context ?=> TermSymbol =
    Symbols.newAnonFun(owner, continuation.info)

  private lazy val intType: Context ?=> TypeRef =
    summon[Context].definitions.IntType

  private lazy val right: Context ?=> Symbol =
    Symbols.requiredModule("scala.util.Right")

  private lazy val continuationVal: Context ?=> Symbol =
    Symbols.newSymbol(
      owner,
      Names.termName("continuation"),
      EmptyFlags,
      continuation.typeRef.appliedTo(intType)
    )

  private lazy val rightOne: Context ?=> Apply =
    ref(right)
      .select(Names.termName("apply"))
      .appliedToTypes(List(c.definitions.ThrowableType, intType))
      .appliedTo(one)

  lazy val inlinedCallToContinuationsSuspendOfInt: Context ?=> Inlined =
    Inlined(
      Apply(
        TypeApply(
          Apply(
            ref(suspend).select(Names.termName("shift")),
            List(ref(suspend))
          ),
          List(TypeTree(intType))),
        List(
          Block(
            Nil,
            Block(
              List(
                DefDef(
                  anonFunc,
                  List(List(continuationVal)),
                  c.definitions.UnitType,
                  Block(
                    Nil,
                    ref(continuationVal).select(Names.termName("resume")).appliedTo(rightOne)
                  )
                )),
              Closure(Nil, ref(anonFunc), TypeTree(c.definitions.UnitType))
            )
          ))
      ),
      List(),
      Typed(
        Ident(requiredMethod("scala.Predef.???").typeRef),
        TypeTree(c.definitions.IntType)
      )
    )

  lazy val inlinedCallToContinuationsSuspendOfIntNotInLastRow: Context ?=> Block =
    Block(List(inlinedCallToContinuationsSuspendOfInt), Literal(Constant(10)))

  val flattenableNestedBlock = FunFixture[Context ?=> Block](
    setup = _ => { Block(List(), Block(List(), Literal(Constant(1)))) },
    teardown = _ => ())

  val expectedFlattenedBlock = FunFixture[Context ?=> Block](
    setup = _ => Block(List(), Literal(Constant(1))),
    teardown = _ => ())

  val recursiveNestedBlock = FunFixture[Context ?=> Block](
    setup = _ => { Block(Nil, (Block(Nil, Block(Nil, Literal(Constant(1)))))) },
    teardown = _ => ())

  val unflattenableNestedBlock = FunFixture[Context ?=> Block](
    setup = _ => {
      Block(
        List(),
        Block(
          List(
            Lambda(
              MethodType.apply(List(defn.ThrowableType))(_ => defn.NothingType),
              trees => Throw(trees.head))),
          Literal(Constant(1))))
    },
    teardown = _ => ()
  )

  val continuationsContextAndFlattenableNestedBlock =
    FunFixture.map3(
      compilerContextWithContinuationsPlugin,
      flattenableNestedBlock,
      expectedFlattenedBlock)

  val continuationsContextAndUnflattenableNestedBlock =
    FunFixture.map2(compilerContextWithContinuationsPlugin, unflattenableNestedBlock)

  val continuationsContextAndFlattenableRecursiveBlock =
    FunFixture.map3(
      compilerContextWithContinuationsPlugin,
      recursiveNestedBlock,
      expectedFlattenedBlock)

  private lazy val suspend: Context ?=> Symbol =
    Symbols.requiredClass("continuations.Suspend")

  private lazy val usingSuspend: Context ?=> Symbol =
    val c = summon[Context]
    Symbols.newSymbol(
      owner,
      Names.termName("x$1"),
      Flags.union(Flags.GivenOrImplicit, Flags.Param),
      suspendType
    )
}
