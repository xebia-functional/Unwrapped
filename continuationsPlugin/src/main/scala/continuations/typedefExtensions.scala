package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*

extension (td: TypeDef)
  def buildInitializer(owner: Symbol)(using ctx: Context): ValDef =
    ValDef(
      newSymbol(
        owner,
        $continuationName.sliceToTermName(0, $continuationName.size),
        Flags.Local,
        td.symbol.info),
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
              newSymbol(owner, nme.x_0, Flags.Case | Flags.CaseAccessor, defn.AnyType).entered
            CaseDef(
              Typed(ref(x$0), ref(td.symbol)),
              ref(x$0)
                .select($labelName.sliceToTermName(0, $labelName.size))
                .select(defn.IntClass.requiredMethod(nme.BitwiseAnd))
                .appliedTo(ref(requiredModuleRef("scala.Int").select(
                  minValueName.sliceToTermName(0, minValueName.size))))
                .select(defn.IntClass.requiredMethod(nme.NE, List(defn.IntType)))
                .appliedTo(Literal(Constant(0))),
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
            Underscore(defn.AnyType),
            EmptyTree,
            New(
              td.symbol.info,
              List(
                ref(
                  owner
                    .paramSymss
                    .flatten
                    .iterator
                    .findSymbol(_.info.hasClassSymbol(requiredClass(continuationFullName))))))
          )
        )
      )
    )
