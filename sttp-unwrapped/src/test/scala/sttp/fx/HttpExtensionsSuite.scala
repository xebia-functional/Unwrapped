package sttp
package unwrapped

import munit.unwrapped.UnwrappedSuite
import java.net.URI

class HttpExtensionsSuite extends UnwrappedSuite, HttpExtensionsSuiteFixtures {

  uri.testUnwrapped("uri.patch should return a PatchPartiallyApplied") { uri =>
    assertEqualsUnwrapped(uri.patch[String], PatchPartiallyApplied[String](uri))
  }

  uri.testUnwrapped("uri.post should return a PostPartiallyApplied") { uri =>
    assertEqualsUnwrapped(uri.post[String], PostPartiallyApplied[String](uri))
  }

  uri.testUnwrapped("uri.put should return a PutPartiallyApplied") { uri =>
    assertEqualsUnwrapped(uri.put[String], PutPartiallyApplied(uri))
  }

}
