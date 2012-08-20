package edu.umass.cs.iesl.namejuggler

import com.weiglewilczek.slf4s.Logging
import org.scalatest.{FunSuite, BeforeAndAfter}
import edu.umass.cs.iesl.scalacommons.{NonemptyString, StringUtils}
import StringUtils._

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
class PersonNameTest extends FunSuite with BeforeAndAfter with Logging {
  import StringUtils.enrichString

	private def assertCanonicalCompatible(a: String, b: String) {
		assert(PersonNameWithDerivations(a).toCanonical compatibleWith PersonNameWithDerivations(b).toCanonical)
	}

  /*
  private def assertInferredCompatible(a: String, b: String) {
    assert(PersonNameWithDerivations(a).inferFully compatibleWith PersonNameWithDerivations(b).inferFully)
  }*/

  private def assertNotCanonicalCompatible(a: String, b: String) {
		assert(!(PersonNameWithDerivations(a).toCanonical compatibleWith PersonNameWithDerivations(b).toCanonical))
	}

	test("Names invert without space") {
		                                   assert(PersonNameWithDerivations("Smith,John").inferFully.bestFullName equals emptyStringToNone("John Smith"))
	                                   }
	test("Names invert with space") {
		                                assert(PersonNameWithDerivations("Smith, John").inferFully.bestFullName equals emptyStringToNone("John Smith"))
	                                }
	test("Names don't invert with zero commas") {
		                                            assert(PersonNameWithDerivations("John Smith").inferFully.bestFullName equals
		                                                   emptyStringToNone("John Smith"))
	                                            }
	test("Names don't invert with two commas, easy degree") {
		                                           assert(PersonNameWithDerivations("Smith, John, Ph.D.").inferFully.bestFullName equals
		                                                  emptyStringToNone("John Smith, Ph.D."))
	                                           }

	test("Names don't invert with two commas, hard degree") {
		                                           assert(PersonNameWithDerivations("Smith, John, PhD").inferFully.bestFullName equals
		                                                  emptyStringToNone("John Smith, PhD"))
	                                           }

	test("Names may match fully") {assertCanonicalCompatible("John Smith", "John Smith")}

	test("Inverted names match") {assertCanonicalCompatible("Smith, John", "John Smith")}

	test("Last names must match") {assertNotCanonicalCompatible("John Smith", "John Jones")}

	test("Suffixes supported without comma") {assertCanonicalCompatible("John Smith", "John Smith MD")}

	test("Suffixes supported with comma") {assertCanonicalCompatible("John Smith", "John Smith, MD")}

	test("First names must match") {assertNotCanonicalCompatible("John Smith", "Jane Smith")}

	test("First names may match as initial") {assertCanonicalCompatible("John Smith", "J Smith")}

	test("First names may match as initial, inverted") {assertCanonicalCompatible("Smith, John", "J Smith")}

	test("Adding middle name OK") {assertCanonicalCompatible("John Smith", "John Archibald Smith")}

	test("Adding middle initial OK") {assertCanonicalCompatible("John Smith", "John A Smith")}

	test("Adding middle name OK with first initial") {assertCanonicalCompatible("John Smith", "J Archibald Smith")}

	test("Adding middle initial OK with first initial") {assertCanonicalCompatible("John Smith", "J A Smith")}

	test("Wrong middle initials are incompatible") {assertNotCanonicalCompatible("John A Smith", "John B Smith")}

	test("Adding joined middle initial OK with first initial") {assertCanonicalCompatible("John Smith", "JA Smith")}

	test("Initial vs joined middle initial are compatible") {assertCanonicalCompatible("J Smith", "JA Smith")}

	test("Wrong joined middle initials are incompatible") {assertNotCanonicalCompatible("JA Smith", "JB Smith")}

	test("Short first name interpreted as name, not initials, when not solid caps") {assertCanonicalCompatible("E O Wilson", "Ed Wilson")}

	test("Solid caps short first name interpreted as initials")  {assertCanonicalCompatible("E O Wilson", "EO Wilson")}

	test("Solid caps short first name not interpreted as initials when there are other initals")  {assertCanonicalCompatible("E O Wilson", "ED O Wilson")}

	test("Solid caps long first name interpreted as name")  {assertCanonicalCompatible("E O Wilson", "EDWARD Wilson")}

	test("Correct joined middle initial is compatible with full name") {assertCanonicalCompatible("Edward O. Wilson", "EO Wilson")}

	test("Wrong joined middle initial is incompatible with full name") {assertNotCanonicalCompatible("Edward O. Wilson", "EQ Wilson")}

  test("Lower-case middle names are interpreted as particles") {assert(PersonNameWithDerivations("Jacqueline du Pre").toCanonical.surNames.contains(NonemptyString("du Pre")))}

  test("Names with particles are compatible with names missing particles") {assertCanonicalCompatible("Jacqueline du Pre", "Jacqueline Pre")}

  // https://github.com/AnaMarjanica/name-compare/blob/master/src/test/scala/hr/element/etb/name_compare/FuzzyStringTest.scala
  test("Names are compatible with deaccented versions") { assertCanonicalCompatible("Ana Durić", "Ana Duric")}

  test("Names with particles and accents are compatible with simplified variant") {assertCanonicalCompatible("Jacqueline du Pré", "Jacqueline Pre")}


//  test("With inference, ames with particles are compatible with names missing particles") {assertInferredCompatible("Jacqueline du Pre", "Jacqueline Pre")}

  // https://github.com/AnaMarjanica/name-compare/blob/master/src/test/scala/hr/element/etb/name_compare/FuzzyStringTest.scala
  //test("With inference, names are compatible with deaccented versions") { assertInferredCompatible("Ana Đurić", "Ana Duric")}
  // actually we can't deal with the funky D unless we switch the transliterator to icu4j
//  test("With inference, names are compatible with deaccented versions") { assertInferredCompatible("Ana Đurić", "Ana Duric")}

}
