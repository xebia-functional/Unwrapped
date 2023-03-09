package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.SnapshotSuite

class NonCompanionObjectSnapshotsSuite extends SnapshotSuite, CompilerFixtures:

  compilerContextWithContinuationsPlugin.snapshotTest("test-1") {
    case given Context =>
      val source = loadFile("NonCompanionObject1")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-2") {
    case given Context =>
      val source = loadFile("NonCompanionObject2")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-3") {
    case given Context =>
      val source = loadFile("NonCompanionObject3")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-4") {
    case given Context =>
      val source = loadFile("NonCompanionObject4")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-5") {
    case given Context =>
      val source = loadFile("NonCompanionObject5")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-6") {
    case given Context =>
      val source = loadFile("NonCompanionObject6")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-7") {
    case given Context =>
      val source = loadFile("NonCompanionObject7")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-8") {
    case given Context =>
      val source = loadFile("NonCompanionObject8")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-9") {
    case given Context =>
      val source = loadFile("NonCompanionObject9")

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-10") {
    case given Context =>
      val source = loadFile("NonCompanionObject10")

      continuationsCompilerSnapshot(source)
  }
