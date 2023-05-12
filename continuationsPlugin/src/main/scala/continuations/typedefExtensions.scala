package continuations

import continuations.ContinuationsPhase.ContinuationValMatchVal
import continuations.ContinuationsPhase.ContinuationValMatchValKey
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*

extension (td: TypeDef)
  private[continuations] def buildInitializer(fromTree: tpd.Tree)(using ctx: Context): ValDef =
    val owner = fromTree.symbol
    val initializationMatchTree = ValDef(
      newSymbol(
        owner,
        $continuationName.sliceToTermName(0, $continuationName.size),
        Flags.Local,
        td.tpe
      ).entered.asTerm,
      Match(
        ref(
          owner
            .paramSymss
            .flatten
            .iterator
            .findSymbol(_.info.hasClassSymbol(requiredClass(continuationFullName)))),
        List(
          {
            val x$0 =
              newSymbol(owner, nme.x_0, Flags.Case | Flags.CaseAccessor, td.tpe).entered.asTerm
            CaseDef(
              tpd.BindTyped(x$0, td.tpe),
              ref(x$0)
                .select($labelName.sliceToTermName(0, $labelName.size))
                .select(defn.IntClass.requiredMethod(nme.AND, List(defn.IntType)))
                .appliedTo(ref(requiredModuleRef("scala.Int").select(
                  minValueName.sliceToTermName(0, minValueName.size))))
                .select(defn.IntClass.requiredMethod(nme.NE, List(defn.IntType)))
                .appliedTo(Literal(Constant(0x0))),
              Block(
                List(
                  Assign(
                    ref(x$0).select($labelName.sliceToTermName(0, $labelName.size)),
                    ref(x$0)
                      .select($labelName.sliceToTermName(0, $labelName.size))
                      .select(defn.Int_-)
                      .appliedTo(ref(requiredModuleRef("scala.Int").select(
                        minValueName.sliceToTermName(0, minValueName.size))))
                  )),
                ref(x$0)
              )
            )
          },
            CaseDef(
              Underscore(ctx.definitions.AnyType),
              EmptyTree,
                New(td.tpe, List(
                  ref(
                    owner
                      .paramSymss
                      .flatten
                      .iterator
                      .findSymbol(_.info.hasClassSymbol(requiredClass(continuationFullName)))))))
        )
      )
    )
    fromTree.putAttachment(
      ContinuationValMatchValKey,
      ContinuationValMatchVal(initializationMatchTree))
    println(
      s"fromTree.getAttachment(ContinuationValMatchValKey).exists: ${fromTree.getAttachment(ContinuationValMatchValKey).isDefined}")
    println(s"initializationMatchTree: ${initializationMatchTree.show}")
    initializationMatchTree
