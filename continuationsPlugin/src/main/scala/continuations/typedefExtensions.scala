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
      ).entered.asTerm, {
        val completionParam = ref(
          owner
            .paramSymss
            .flatten
            .iterator
            .findSymbol(_.info.hasClassSymbol(requiredClass(continuationFullName))))
        If(
          completionParam
            .select(nme.isInstanceOf_)
            .appliedToType(td.tpe)
            .select(nme.ZAND)
            .appliedTo(
              completionParam
                .select(nme.asInstanceOf_)
                .appliedToType(td.tpe)
                .select($labelName.sliceToTermName(0, $labelName.size))
                .select(defn.IntClass.requiredMethod(nme.AND, List(defn.IntType)))
                .appliedTo(ref(requiredModuleRef("scala.Int").select(
                  minValueName.sliceToTermName(0, minValueName.size))))
                .select(defn.IntClass.requiredMethod(nme.NE, List(defn.IntType)))
                .appliedTo(Literal(Constant(0x0)))),
          Block(
            List(
              Assign(
                completionParam
                  .select(nme.asInstanceOf_)
                  .appliedToType(td.tpe)
                  .select($labelName.sliceToTermName(0, $labelName.size)),
                completionParam
                  .select(nme.asInstanceOf_)
                  .appliedToType(td.tpe)
                  .select($labelName.sliceToTermName(0, $labelName.size))
                  .select(defn.Int_-)
                  .appliedTo(ref(requiredModuleRef("scala.Int").select(
                    minValueName.sliceToTermName(0, minValueName.size))))
              )),
            completionParam
          ),
          New(td.tpe, List(completionParam))
        )
      }
    )
    fromTree.putAttachment(
      ContinuationValMatchValKey,
      ContinuationValMatchVal(initializationMatchTree))
    println(
      s"fromTree.getAttachment(ContinuationValMatchValKey).exists: ${fromTree.getAttachment(ContinuationValMatchValKey).isDefined}")
    println(s"initializationMatchTree: ${initializationMatchTree.show}")
    initializationMatchTree
