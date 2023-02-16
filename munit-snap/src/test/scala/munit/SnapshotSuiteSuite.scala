package munit

import io.circe._
import io.circe.generic.AutoDerivation
import io.circe.syntax._

class SnapshotSuiteSuite extends SnapshotSuite, AutoDerivation, SnapshotTestFixtures:

  snapshotTest("It should pass a snapshot test") { 1 + 1 }

  snapshotTest("It should fail a snapshot test".fail) { 2 + 2 }

  providedFixture.snapshotTest("It should pass with a fixture") { _ => 4 + 1 }

  providedFixture.snapshotTest("It should fail with a fixture".fail) { _ => 4 + 0 }

  providedFixture.snapshotTest("It should clear".clear) { _ => 3 + 3 }
  providedFixture.snapshotTest("It should clear out a fixture".clear) { _ => 3 + 3 }
