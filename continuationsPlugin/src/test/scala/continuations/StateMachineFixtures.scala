package continuations

import scala.io.Source

trait StateMachineFixtures {

  def loadFile(file: String): String = Source.fromResource(s"$file.scala").mkString

  val expectedOneSuspendContinuation =
    loadFile("OneSuspendContinuation")

  val expectedOneSuspendContinuationTwoBlocks =
    loadFile("OneSuspendContinuationTwoBlocks")

  val expectedOneSuspendContinuationThreeBlocks =
    loadFile("OneSuspendContinuationThreeBlocks")

  val expectedOneSuspendContinuationFourBlocks =
    loadFile("OneSuspendContinuationFourBlocks")

  val expectedStateMachineForSuspendContinuationReturningANonSuspendingVal =
    loadFile("StateMachineForSuspendContinuationReturningANonSuspendingVal")

  val expectedStateMachineWithSingleSuspendedContinuationReturningANonSuspendedVal =
    loadFile("StateMachineWithSingleSuspendedContinuationReturningANonSuspendedVal")

  val expectedStateMachineMultipleSuspendedContinuationsReturningANonSuspendingVal =
    loadFile("StateMachineMultipleSuspendedContinuationsReturningANonSuspendingVal")

  val expectedStateMachineWithMultipleResumeReturningANonSuspendedValue =
    loadFile("StateMachineWithMultipleResumeReturningANonSuspendedValue")

  val expectedStateMachineReturningANonSuspendedValue =
    loadFile("StateMachineReturningANonSuspendedValue")

  val expectedStateMachineNoDependantSuspensions =
    loadFile("StateMachineNoDependantSuspensions")

  val expectedStateMachineNoDependantSuspensionsWithCodeInside =
    loadFile("StateMachineNoDependantSuspensionsWithCodeInside")

  val expectedStateMachineNoDependantSuspensionsWithCodeBetween =
    loadFile("StateMachineNoDependantSuspensionsWithCodeBetween")

  val expectedStateMachineOneParamOneDependantContinuation =
    loadFile("StateMachineOneParamOneDependantContinuation")

  val expectedStateMachineOneParamOneNoDependantContinuation =
    loadFile("StateMachineOneParamOneNoDependantContinuation")

  val expectedStateMachineNoParamOneNoDependantContinuationCodeBeforeUsedAfter =
    loadFile("StateMachineNoParamOneNoDependantContinuationCodeBeforeUsedAfter")

  val expectedStateMachineManyDependantContinuations =
    loadFile("StateMachineManyDependantContinuations")

  val expectedStateMachineManyDependantAndNoDependantContinuations =
    loadFile("StateMachineManyDependantAndNoDependantContinuations")

  val expectedStateMachineWithDependantAndNoDependantContinuationAtTheEnd =
    loadFile("StateMachineWithDependantAndNoDependantContinuationAtTheEnd")

  val expectedStateMachineForOneChainedContinuation =
    loadFile("StateMachineForOneChainedContinuation")

  val expectedStateMachineMultipleChainedSuspendContinuationsReturningANonSuspendedVal =
    loadFile("StateMachineMultipleChainedSuspendContinuationsReturningANonSuspendedVal")

  val expectedStateMachineChainedSuspendContinuationsOneParameter =
    loadFile("StateMachineChainedSuspendContinuationsOneParameter")

  val expectedStateMachineChainedSuspendContinuationsOneParameterAndVals =
    loadFile("StateMachineChainedSuspendContinuationsOneParameterAndVals")

  val expectedStateMachineTwoContinuationsChained =
    loadFile("StateMachineTwoContinuationsChained")

  val expectedStateMachineTwoContinuationsChainedOneGenericParam =
    loadFile("StateMachineTwoContinuationsChainedOneGenericParam")

  val expectedStateMachineTwoContinuationsChainedTwoGenericParams =
    loadFile("StateMachineTwoContinuationsChainedTwoGenericParams")

  val expectedStateMachineTwoContinuationsChainedExtraGivenParam =
    loadFile("StateMachineTwoContinuationsChainedExtraGivenParam")

  val expectedStateMachineContextFunctionTwoContinuationsChainedExtraGivenParam =
    loadFile("StateMachineContextFunctionTwoContinuationsChainedExtraGivenParam")

  val expectedStateMachineContextFunctionTwoContinuationsChained =
    loadFile("StateMachineContextFunctionTwoContinuationsChained")

  val expectedStateMachineTwoContinuationsChainedOneLinePrior =
    loadFile("StateMachineTwoContinuationsChainedOneLinePrior")

  val expectedStateMachineTwoContinuationsChainedOneValPrior =
    loadFile("StateMachineTwoContinuationsChainedOneValPrior")

  val expectedStateMachineTwoContinuationsChainedTwoLinesPrior =
    loadFile("StateMachineTwoContinuationsChainedTwoLinesPrior")

  val expectedStateMachineTwoContinuationsChainedTwoLinesOneValPrior =
    loadFile("StateMachineTwoContinuationsChainedTwoLinesOneValPrior")

  val expectedStateMachineTwoContinuationsChainedTwoValPrior =
    loadFile("StateMachineTwoContinuationsChainedTwoValPrior")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorOneLineBetween =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorOneLineBetween")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorOneValBetween =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorOneValBetween")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoLinesBetween =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoLinesBetween")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoLinesOneValBetween =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoLinesOneValBetween")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetween =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetween")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneLineAfter =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneLineAfter")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneValAfter =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneValAfter")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoLinesAfter =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoLinesAfter")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfter =
    loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfter")

  val expectedStateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfterChainIgnored =
    loadFile(
      "StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfterChainIgnored")

}
