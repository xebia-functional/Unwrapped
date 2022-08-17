package sttp
package fx

import munit.fx.ScalaFXSuite
import java.net.URI

class HttpExtensionsSuite extends ScalaFXSuite, HttpExtensionsSuiteFixtures {

  uri.testFX("uri.patch should return a PatchPartiallyApplied") { uri =>
    assertEqualsFX(uri.patch[String], PatchPartiallyApplied[String](uri))
  }

  uri.testFX("uri.post should return a PostPartiallyApplied") { uri =>
    assertEqualsFX(uri.post[String], PostPartiallyApplied[String](uri))
  }

  uri.testFX("uri.put should return a PutPartiallyApplied"){ uri =>
    assertEqualsFX(uri.put[String], PutPartiallyApplied(uri))
  }

}
