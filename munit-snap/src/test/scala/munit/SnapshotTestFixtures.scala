package munit

import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths

trait SnapshotTestFixtures { self: SnapshotSuiteSuite =>
  val providedFixture = FunFixture[Int](
    setup = _ => {
      5
    },
    teardown = _ => ()
  )

}
