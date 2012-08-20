package edu.umass.cs.iesl.namejuggler

import com.weiglewilczek.slf4s.Logging
import org.scalatest.{FunSuite, BeforeAndAfter}
import edu.umass.cs.iesl.scalacommons.StringUtils
import StringUtils._

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
class PersonNameTest extends FunSuite with BeforeAndAfter with Logging {

	private def assertCompatible(a: String, b: String) {
		assert(PersonNameWithDerivations(a).toCanonical compatibleWith PersonNameWithDerivations(b).toCanonical)
	}

	private def assertNotCompatible(a: String, b: String) {
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

	test("Names may match fully") {assertCompatible("John Smith", "John Smith")}

	test("Inverted names match") {assertCompatible("Smith, John", "John Smith")}

	test("Last names must match") {assertNotCompatible("John Smith", "John Jones")}

	test("Suffixes supported without comma") {assertCompatible("John Smith", "John Smith MD")}

	test("Suffixes supported with comma") {assertCompatible("John Smith", "John Smith, MD")}

	test("First names must match") {assertNotCompatible("John Smith", "Jane Smith")}

	test("First names may match as initial") {assertCompatible("John Smith", "J Smith")}

	test("First names may match as initial, inverted") {assertCompatible("Smith, John", "J Smith")}

	test("Adding middle name OK") {assertCompatible("John Smith", "John Archibald Smith")}

	test("Adding middle initial OK") {assertCompatible("John Smith", "John A Smith")}

	test("Adding middle name OK with first initial") {assertCompatible("John Smith", "J Archibald Smith")}

	test("Adding middle initial OK with first initial") {assertCompatible("John Smith", "J A Smith")}

	test("Wrong middle initials are incompatible") {assertNotCompatible("John A Smith", "John B Smith")}

	test("Adding joined middle initial OK with first initial") {assertCompatible("John Smith", "JA Smith")}

	test("Initial vs joined middle initial are compatible") {assertCompatible("J Smith", "JA Smith")}

	test("Wrong joined middle initials are incompatible") {assertNotCompatible("JA Smith", "JB Smith")}

	test("Short first name interpreted as name, not initials, when not solid caps") {assertCompatible("E O Wilson", "Ed Wilson")}

	test("Solid caps short first name interpreted as initials")  {assertCompatible("E O Wilson", "EO Wilson")}

	test("Solid caps short first name not interpreted as initials when there are other initals")  {assertCompatible("E O Wilson", "ED O Wilson")}

	test("Solid caps long first name interpreted as name")  {assertCompatible("E O Wilson", "EDWARD Wilson")}

	test("Correct joined middle initial is compatible with full name") {assertCompatible("Edward O. Wilson", "EO Wilson")}

	test("Wrong joined middle initial is incompatible with full name") {assertNotCompatible("Edward O. Wilson", "EQ Wilson")}
}
