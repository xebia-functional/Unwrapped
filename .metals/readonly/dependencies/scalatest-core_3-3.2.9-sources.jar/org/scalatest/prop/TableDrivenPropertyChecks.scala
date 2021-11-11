/*
 * Copyright 2001-2021 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.prop

import org.scalatest.exceptions.StackDepth
import scala.annotation.tailrec
import org.scalatest.enablers.TableAsserting
import org.scalactic._

/**
 * Trait containing methods that faciliate property checks against tables of data.
 *
 * <p>
 * This trait contains one <code>exists</code>, <code>forAll</code>, and <code>forEvery</code> method for each <code>TableForN</code> class, <code>TableFor1</code>
 * through <code>TableFor22</code>, which allow properties to be checked against the rows of a table. It also
 * contains a <code>whenever</code> method that can be used to indicate a property need only hold whenever some
 * condition is true.
 * </p>
 *
 * <p>
 * For an example of trait <code>TableDrivenPropertyChecks</code> in action, imagine you want to test this <code>Fraction</code> class:
 * </p>
 *
 * <pre class="stHighlight">
 * class Fraction(n: Int, d: Int) {
 *
 *   require(d != 0)
 *   require(d != Integer.MIN_VALUE)
 *   require(n != Integer.MIN_VALUE)
 *
 *   val numer = if (d < 0) -1 * n else n
 *   val denom = d.abs
 *
 *   override def toString = numer + " / " + denom
 * }
 * </pre>
 *
 * <p>
 * <code>TableDrivenPropertyChecks</code> allows you to create tables with
 * between 1 and 22 columns and any number of rows. You create a table by passing
 * tuples to one of the factory methods of object <code>Table</code>. Each tuple must have the
 * same arity (number of members). The first tuple you pass must all be strings, because
 * it defines names for the columns. Subsequent tuples define the data. After the initial tuple
 * that contains string column names, all tuples must have the same type. For example,
 * if the first tuple after the column names contains two <code>Int</code>s, all subsequent
 * tuples must contain two <code>Int</code> (<em>i.e.</em>, have type
 * <code>Tuple2[Int, Int]</code>).
 * </p>
 *
 * <p>
 * To test the behavior of <code>Fraction</code>, you could create a table
 * of numerators and denominators to pass to the constructor of the
 * <code>Fraction</code> class using one of the <code>apply</code> factory methods declared
 * in <code>Table</code>, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.prop.TableDrivenPropertyChecks._
 *
 * val fractions =
 *   Table(
 *     ("n", "d"),  // First tuple defines column names
 *     (  1,   2),  // Subsequent tuples define the data
 *     ( -1,   2),
 *     (  1,  -2),
 *     ( -1,  -2),
 *     (  3,   1),
 *     ( -3,   1),
 *     ( -3,   0),
 *     (  3,  -1),
 *     (  3,  Integer.MIN_VALUE),
 *     (Integer.MIN_VALUE, 3),
 *     ( -3,  -1)
 *   )
 * </pre>
 *
 * <p>
 * You could then check a property against each row of the table using a <code>forAll</code> method, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.matchers.should.Matchers._
 *
 * forAll (fractions) { (n: Int, d: Int) =>
 *
 *   whenever (d != 0 && d != Integer.MIN_VALUE
 *       && n != Integer.MIN_VALUE) {
 *
 *     val f = new Fraction(n, d)
 *
 *     if (n < 0 && d < 0 || n > 0 && d > 0)
 *       f.numer should be > 0
 *     else if (n != 0)
 *       f.numer should be < 0
 *     else
 *       f.numer should be === 0
 *
 *     f.denom should be > 0
 *   }
 * }
 * </pre>
 *
 * <p>
 * Trait <code>TableDrivenPropertyChecks</code> provides 22 overloaded <code>exists</code>, <code>forAll</code>, and <code>forEvery</code> methods
 * that allow you to check properties using the data provided by a table. Each <code>exists</code>, <code>forAll</code>, and <code>forEvery</code>
 * method takes two parameter lists. The first parameter list is a table. The second parameter list
 * is a function whose argument types and number matches that of the tuples in the table. For
 * example, if the tuples in the table supplied to <code>forAll</code> each contain an
 * <code>Int</code>, a <code>String</code>, and a <code>List[Char]</code>, then the function supplied
 * to <code>forAll</code> must take 3 parameters, an <code>Int</code>, a <code>String</code>,
 * and a <code>List[Char]</code>. The <code>forAll</code> method will pass each row of data to
 * the function, and generate a <code>TableDrivenPropertyCheckFailedException</code> if the function
 * completes abruptly for any row of data with any exception that would <a href="../Suite.html#errorHandling">normally cause</a> a test to
 * fail in ScalaTest other than <code>DiscardedEvaluationException</code>. A
 * <code>DiscardedEvaluationException</code>,
 * which is thrown by the <code>whenever</code> method (also defined in this trait) to indicate
 * a condition required by the property function is not met by a row
 * of passed data, will simply cause <code>forAll</code> to skip that row of data.
 * <p>
 *
 * <p>
 * The full list of table methods are:
 * </p>
 *
 * <ul>
 * <li><code>exists</code> - succeeds if the assertion holds true for at least one element</li>
 * <li><code>forAll</code> - succeeds if the assertion holds true for every element</li>
 * <li><code>forEvery</code> - same as <code>forAll</code>, but lists all failing elements if it fails (whereas
 *    <code>forAll</code> just reports the first failing element) and throws <code>TestFailedException</code> with
 *    the first failed check as the cause.</li>
 * </ul>
 *
 * <a name="testingStatefulFunctions"></a><h2>Testing stateful functions</h2>
 *
 * <p>
 * One way to use a table with one column is to test subsequent return values
 * of a stateful function. Imagine, for example, you had an object named <code>FiboGen</code>
 * whose <code>next</code> method returned the <em>next</em> fibonacci number, where next
 * means the next number in the series following the number previously returned by <code>next</code>.
 * So the first time <code>next</code> was called, it would return 0. The next time it was called
 * it would return 1. Then 1. Then 2. Then 3, and so on. <code>FiboGen</code> would need to
 * maintain state, because it has to remember where it is in the series. In such a situation,
 * you could create a <code>TableFor1</code> (a table with one column, which you could alternatively
 * think of as one row), in which each row represents
 * the next value you expect.
 * </p>
 *
 * <pre class="stHighlight">
 * val first14FiboNums =
 *   Table("n", 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233)
 * </pre>
 *
 * <p>
 * Then in your <code>forAll</code> simply call the function and compare it with the
 * expected return value, like this:
 * </p>
 *
 * <pre class="stHighlight">
 *  forAll (first14FiboNums) { n =>
 *    FiboGen.next should equal (n)
 *  }
 * </pre>
 *
 * <a name="testingMutables"></a><h2>Testing mutable objects</h2>
 *
 * <p>
 * If you need to test a mutable object, one way you can use tables is to specify
 * state transitions in a table. For example, imagine you wanted to test this mutable
 * <code>Counter</code> class:
 *
 * <pre class="stHighlight">
      class Counter {
        private var c = 0
        def reset() { c = 0 }
        def click() { c += 1 }
        def enter(n: Int) { c = n }
        def count = c
      }
 * </pre>
 *
 * <p>
 * A <code>Counter</code> keeps track of how many times its <code>click</code> method
 * is called. The count starts out at zero and increments with each <code>click</code>
 * invocation. You can also set the count to a specific value by calling <code>enter</code>
 * and passing the value in. And the <code>reset</code> method returns the count back to
 * zero. You could define the actions that initiate state transitions with case classes, like this:
 * </p>
 *
 * <pre class="stHighlight">
      abstract class Action
      case object Start extends Action
      case object Click extends Action
      case class Enter(n: Int) extends Action
 * </pre>
 *
 * <p>
 * Given these actions, you could define a state-transition table like this:
 * </p>
 *
 * <pre class="stHighlight">
      val stateTransitions =
        Table(
          ("action", "expectedCount"),
          (Start,    0),
          (Click,    1),
          (Click,    2),
          (Click,    3),
          (Enter(5), 5),
          (Click,    6),
          (Enter(1), 1),
          (Click,    2),
          (Click,    3)
        )
 * </pre>
 *
 * <p>
 * To use this in a test, simply do a pattern match inside the function you pass
 * to <code>forAll</code>. Make a pattern for each action, and have the body perform that
 * action when there's a match. Then check that the actual value equals the expected value:
 * </p>
 *
 * <pre class="stHighlight">
      val counter = new Counter
      forAll (stateTransitions) { (action, expectedCount) =>
        action match {
          case Start => counter.reset()
          case Click => counter.click()
          case Enter(n) => counter.enter(n)
        }
        counter.count should equal (expectedCount)
      }
 * </pre>
 *
 * <a name="invalidArgCombos"></a><h2>Testing invalid argument combinations</h2>
 *
 * <p>
 * A table-driven property check can also be helpful to ensure that the proper exception is thrown when invalid data is
 * passed to a method or constructor. For example, the <code>Fraction</code> constructor shown above should throw <code>IllegalArgumentException</code>
 * if <code>Integer.MIN_VALUE</code> is passed for either the numerator or denominator, or zero is passed for the denominator. This yields the
 * following five combinations of invalid data:
 * </p>
 *
 * <table style="border-collapse: collapse; border: 1px solid black">
 * <tr><th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black"><code>n</code></th><th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black"><code>d</code></th></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center"><code>Integer.MIN_VALUE</code></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center"><code>Integer.MIN_VALUE</code></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">a valid value</td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center"><code>Integer.MIN_VALUE</code></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center"><code>Integer.MIN_VALUE</code></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">a valid value</td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center"><code>Integer.MIN_VALUE</code></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">zero</td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">a valid value</td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">zero</td></tr>
 * </table>
 *
 * <p>
 * You can express these combinations in a table:
 * </p>
 *
 * <pre class="stHighlight">
 * val invalidCombos =
 *   Table(
 *     ("n",               "d"),
 *     (Integer.MIN_VALUE, Integer.MIN_VALUE),
 *     (1,                 Integer.MIN_VALUE),
 *     (Integer.MIN_VALUE, 1),
 *     (Integer.MIN_VALUE, 0),
 *     (1,                 0)
 *   )
 * </pre>
 *
 * <p>
 * Given this table, you could check that all invalid combinations produce <code>IllegalArgumentException</code>, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * forAll (invalidCombos) { (n: Int, d: Int) =>
 *   evaluating {
 *     new Fraction(n, d)
 *   } should produce [IllegalArgumentException]
 * }
 * </pre>
 *
 * </p>
 * @author Bill Venners
 */
trait TableDrivenPropertyChecks extends Whenever with Tables {

  /*
   * Evaluates the passed code block if the passed boolean condition is true, else throws <code>DiscardedEvaluationException</code>.
   *
   * <p>
   * The <code>whenever</code> method can be used inside property check functions to skip invocations of the function with
   * data for which it is known the property would fail. For example, given the following <code>Fraction</code> class:
   * </p>
   *
   * <pre class="stHighlight">
   * class Fraction(n: Int, d: Int) {
   *
   *   require(d != 0)
   *   require(d != Integer.MIN_VALUE)
   *   require(n != Integer.MIN_VALUE)
   *
   *   val numer = if (d < 0) -1 * n else n
   *   val denom = d.abs
   *
   *   override def toString = numer + " / " + denom
   * }
   * </pre>
   *
   * <p>
   * You could create a table of numerators and denominators to pass to the constructor of the
   * <code>Fraction</code> class like this:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.prop.TableDrivenPropertyChecks._
   *
   * val fractions =
   *   Table(
   *     ("n", "d"),
   *     (  1,   2),
   *     ( -1,   2),
   *     (  1,  -2),
   *     ( -1,  -2),
   *     (  3,   1),
   *     ( -3,   1),
   *     ( -3,   0),
   *     (  3,  -1),
   *     (  3,  Integer.MIN_VALUE),
   *     (Integer.MIN_VALUE, 3),
   *     ( -3,  -1)
   *   )
   * </pre>
   *
   * <p>
   * Imagine you wanted to check a property against this class with data that includes some
   * value that are rejected by the constructor, such as a denominator of zero, which should
   * result in an <code>IllegalArgumentException</code>. You could use <code>whenever</code>
   * to skip any rows in the <code>fraction</code> that represent illegal arguments, like this:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.Matchers._
   *
   * forAll (fractions) { (n: Int, d: Int) =>
   *
   *   whenever (d != 0 && d != Integer.MIN_VALUE
   *       && n != Integer.MIN_VALUE) {
   *
   *     val f = new Fraction(n, d)
   *
   *     if (n < 0 && d < 0 || n > 0 && d > 0)
   *       f.numer should be > 0
   *     else if (n != 0)
   *       f.numer should be < 0
   *     else
   *       f.numer should be === 0
   *
   *     f.denom should be > 0
   *   }
   * }
   * </pre>
   *
   * <p>
   * In this example, rows 6, 8, and 9 have values that would cause a false to be passed
   * to <code>whenever</code>. (For example, in row 6, <code>d</code> is 0, which means <code>d</code> <code>!=</code> <code>0</code>
   * will be false.) For those rows, <code>whenever</code> will throw <code>DiscardedEvaluationException</code>,
   * which will cause the <code>forAll</code> method to skip that row.
   * </p>
   *
   * @param condition the boolean condition that determines whether <code>whenever</code> will evaluate the
   *    <code>fun</code> function (<code>condition<code> is true) or throws <code>DiscardedEvaluationException</code> (<code>condition<code> is false)
   * @param fun the function to evaluate if the specified <code>condition</code> is true
   */
/*
  def whenever(condition: Boolean)(fun: => Unit) {
    if (!condition)
      throw new DiscardedEvaluationException
    fun
  }
*/

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor1</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, ASSERTION](table: TableFor1[A])(fun: (A) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor2</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, ASSERTION](table: TableFor2[A, B])(fun: (A, B) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor3</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, ASSERTION](table: TableFor3[A, B, C])(fun: (A, B, C) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor4</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, ASSERTION](table: TableFor4[A, B, C, D])(fun: (A, B, C, D) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor5</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, ASSERTION](table: TableFor5[A, B, C, D, E])(fun: (A, B, C, D, E) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor6</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, ASSERTION](table: TableFor6[A, B, C, D, E, F])(fun: (A, B, C, D, E, F) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor7</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, ASSERTION](table: TableFor7[A, B, C, D, E, F, G])(fun: (A, B, C, D, E, F, G) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor8</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, ASSERTION](table: TableFor8[A, B, C, D, E, F, G, H])(fun: (A, B, C, D, E, F, G, H) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor9</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, ASSERTION](table: TableFor9[A, B, C, D, E, F, G, H, I])(fun: (A, B, C, D, E, F, G, H, I) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor10</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, ASSERTION](table: TableFor10[A, B, C, D, E, F, G, H, I, J])(fun: (A, B, C, D, E, F, G, H, I, J) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor11</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, ASSERTION](table: TableFor11[A, B, C, D, E, F, G, H, I, J, K])(fun: (A, B, C, D, E, F, G, H, I, J, K) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor12</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, ASSERTION](table: TableFor12[A, B, C, D, E, F, G, H, I, J, K, L])(fun: (A, B, C, D, E, F, G, H, I, J, K, L) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor13</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, ASSERTION](table: TableFor13[A, B, C, D, E, F, G, H, I, J, K, L, M])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor14</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, ASSERTION](table: TableFor14[A, B, C, D, E, F, G, H, I, J, K, L, M, N])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor15</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, ASSERTION](table: TableFor15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor16</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, ASSERTION](table: TableFor16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor17</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, ASSERTION](table: TableFor17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor18</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, ASSERTION](table: TableFor18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor19</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, ASSERTION](table: TableFor19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor20</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, ASSERTION](table: TableFor20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor21</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, ASSERTION](table: TableFor21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor22</code>.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forAll[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, ASSERTION](table: TableFor22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table(fun)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor1</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, ASSERTION](table: TableFor1[A])(fun: A => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[Tuple1[A], ASSERTION](table.heading, table.map(Tuple1.apply)){a => fun(a._1)}
  }


  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor2</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, ASSERTION](table: TableFor2[A, B])(fun: (A, B) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor3</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, ASSERTION](table: TableFor3[A, B, C])(fun: (A, B, C) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor4</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, ASSERTION](table: TableFor4[A, B, C, D])(fun: (A, B, C, D) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor5</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, ASSERTION](table: TableFor5[A, B, C, D, E])(fun: (A, B, C, D, E) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor6</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, ASSERTION](table: TableFor6[A, B, C, D, E, F])(fun: (A, B, C, D, E, F) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor7</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, ASSERTION](table: TableFor7[A, B, C, D, E, F, G])(fun: (A, B, C, D, E, F, G) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor8</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, ASSERTION](table: TableFor8[A, B, C, D, E, F, G, H])(fun: (A, B, C, D, E, F, G, H) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor9</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, ASSERTION](table: TableFor9[A, B, C, D, E, F, G, H, I])(fun: (A, B, C, D, E, F, G, H, I) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor10</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, ASSERTION](table: TableFor10[A, B, C, D, E, F, G, H, I, J])(fun: (A, B, C, D, E, F, G, H, I, J) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor11</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, ASSERTION](table: TableFor11[A, B, C, D, E, F, G, H, I, J, K])(fun: (A, B, C, D, E, F, G, H, I, J, K) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor12</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, ASSERTION](table: TableFor12[A, B, C, D, E, F, G, H, I, J, K, L])(fun: (A, B, C, D, E, F, G, H, I, J, K, L) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor13</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, ASSERTION](table: TableFor13[A, B, C, D, E, F, G, H, I, J, K, L, M])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor14</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, ASSERTION](table: TableFor14[A, B, C, D, E, F, G, H, I, J, K, L, M, N])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor15</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, ASSERTION](table: TableFor15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor16</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, ASSERTION](table: TableFor16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor17</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, ASSERTION](table: TableFor17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor18</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, ASSERTION](table: TableFor18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor19</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, ASSERTION](table: TableFor19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor20</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, ASSERTION](table: TableFor20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor21</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, ASSERTION](table: TableFor21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor22</code> and reporting every error.
   *
   * <p>
   * The difference between <code>forEvery</code> and <code>forAll</code> is that
   * <code>forEvery</code> will continue to inspect all elements after first failure, and report all failures,
   * whereas <code>forAll</code> will stop on (and only report) the first failure.
   * </p>
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def forEvery[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, ASSERTION](table: TableFor22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.forEvery(fun)
    //asserting.forEvery[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V), ASSERTION](table.heading, table)(fun.tupled)
  }

  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor1</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, ASSERTION](table: TableFor1[A])(fun: A => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[Tuple1[A], ASSERTION](List(table.heading), table.map(Tuple1.apply), Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3){a => fun(a._1)}
  }

                                          
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor2</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, ASSERTION](table: TableFor2[A, B])(fun: (A, B) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor3</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, ASSERTION](table: TableFor3[A, B, C])(fun: (A, B, C) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor4</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, ASSERTION](table: TableFor4[A, B, C, D])(fun: (A, B, C, D) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor5</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, ASSERTION](table: TableFor5[A, B, C, D, E])(fun: (A, B, C, D, E) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor6</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, ASSERTION](table: TableFor6[A, B, C, D, E, F])(fun: (A, B, C, D, E, F) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor7</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, ASSERTION](table: TableFor7[A, B, C, D, E, F, G])(fun: (A, B, C, D, E, F, G) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor8</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, ASSERTION](table: TableFor8[A, B, C, D, E, F, G, H])(fun: (A, B, C, D, E, F, G, H) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor9</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, ASSERTION](table: TableFor9[A, B, C, D, E, F, G, H, I])(fun: (A, B, C, D, E, F, G, H, I) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor10</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, ASSERTION](table: TableFor10[A, B, C, D, E, F, G, H, I, J])(fun: (A, B, C, D, E, F, G, H, I, J) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor11</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, ASSERTION](table: TableFor11[A, B, C, D, E, F, G, H, I, J, K])(fun: (A, B, C, D, E, F, G, H, I, J, K) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor12</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, ASSERTION](table: TableFor12[A, B, C, D, E, F, G, H, I, J, K, L])(fun: (A, B, C, D, E, F, G, H, I, J, K, L) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor13</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, ASSERTION](table: TableFor13[A, B, C, D, E, F, G, H, I, J, K, L, M])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor14</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, ASSERTION](table: TableFor14[A, B, C, D, E, F, G, H, I, J, K, L, M, N])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor15</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, ASSERTION](table: TableFor15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor16</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, ASSERTION](table: TableFor16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor17</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, ASSERTION](table: TableFor17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor18</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, ASSERTION](table: TableFor18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor19</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, ASSERTION](table: TableFor19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor20</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, ASSERTION](table: TableFor20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor21</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, ASSERTION](table: TableFor21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      
  /**
   * Performs a property check by applying the specified property check function to each row
   * of the specified <code>TableFor22</code> and succeeding if at least one element satisfies the property check.
   *
   * @param table the table of data with which to perform the property check
   * @param fun the property check function to apply to each row of data in the table
   */
  def exists[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, ASSERTION](table: TableFor22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V])(fun: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => ASSERTION)(implicit asserting: TableAsserting[ASSERTION], prettifier: Prettifier, pos: source.Position): asserting.Result = {
    table.exists(fun)
    //asserting.exists[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V), ASSERTION](table.heading.productIterator.to[List].map(_.toString), table, Resources.tableDrivenExistsFailed _, "TableDrivenPropertyChecks.scala", "exists", 3)(fun.tupled)
  }
                                      }

/*
 * Companion object that facilitates the importing of <code>TableDrivenPropertyChecks</code> members as
 * an alternative to mixing it in. One use case is to import <code>TableDrivenPropertyChecks</code> members so you can use
 * them in the Scala interpreter:
 *
 * <pre>
 * Welcome to Scala version 2.8.0.final (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_22).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 *
 * scala> import org.scalatest.prop.TableDrivenPropertyChecks._
 * import org.scalatest.prop.TableDrivenPropertyChecks._
 *
 * scala> val examples =
 *   |   Table(
 *   |     ("a", "b"),
 *   |     (  1,   2),
 *   |     (  3,   4)
 *   |   )
 * examples: org.scalatest.prop.TableFor2[Int,Int] = TableFor2((1,2), (3,4))
 *
 * scala> import org.scalatest.Matchers._
 * import org.scalatest.Matchers._
 *
 * scala> forAll (examples) { (a, b) => a should be < b }
 *
 * scala> forAll (examples) { (a, b) => a should be > b }
 * org.scalatest.prop.TableDrivenPropertyCheckFailedException: TestFailedException (included as this exception's cause) was thrown during property evaluation.
 * Message: 1 was not greater than 2
 * Location: <console>:13
 * Occurred at table row 0 (zero based, not counting headings), which had values (
 *   a = 1,
 *   b = 2
 * )
 * at org.scalatest.prop.TableFor2$$anonfun$apply$4.apply(Table.scala:355)
 * at org.scalatest.prop.TableFor2$$anonfun$apply$4.apply(Table.scala:346)
 * at scala.collection.mutable.ResizableArray$class.foreach(ResizableArray.scala:57)
 * at scala.collection.mutable.ListBuffer.foreach(ListBuffer.scala:43)
 * at org.scalatest.prop.TableFor2.apply(Table.scala:346)
 * at org.scalatest.prop.TableDrivenPropertyChecks$class.forAll(TableDrivenPropertyChecks.scala:133)
 * ...
 * </pre>
 *
 * @author Bill Venners
 */
object TableDrivenPropertyChecks extends TableDrivenPropertyChecks
