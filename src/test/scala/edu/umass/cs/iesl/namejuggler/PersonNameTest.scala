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
  import StringUtils._

	private def assertCanonicalCompatible(a: String, b: String) {
    val acan = PersonNameWithDerivations(a).toCanonical
    val bcan = PersonNameWithDerivations(b).toCanonical
    val result = acan compatibleWith bcan
    if (!result)
    {
      logger.error(acan + " not compatible with " + bcan)
    }
    assert(result)
	}

  /*
  private def assertInferredCompatible(a: String, b: String) {
    assert(PersonNameWithDerivations(a).inferFully compatibleWith PersonNameWithDerivations(b).inferFully)
  }*/

  private def assertNotCanonicalCompatible(a: String, b: String) {
    val acan = PersonNameWithDerivations(a).toCanonical
    val bcan = PersonNameWithDerivations(b).toCanonical
    val result = acan compatibleWith bcan
    if (result)
    {
      logger.error(acan + " erroneously compatible with " + bcan)
    }
    assert(!result)
  }

  test("Normally formatted simple names are parsed as expected") {
    val inferred = PersonNameWithDerivations("Kermit T. Frog").inferFully
    assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "Kermit T. Frog".opt)
  }

  test("Initial-formatted simple names with periods and spaces are parsed as expected") {
    val inferred = PersonNameWithDerivations("K. T. Frog").inferFully
    assert(inferred.givenNames === Seq("K.".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "K. T. Frog".opt)
  }

  test("Initial-formatted simple names with periods and no spaces are parsed as expected") {
    val inferred = PersonNameWithDerivations("K.T. Frog").inferFully
    assert(inferred.givenNames === Seq("K.".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "K. T. Frog".opt)
  }

  test("Initial-formatted simple names without periods are parsed as expected") {
    val inferred = PersonNameWithDerivations("KT Frog").inferFully
    assert(inferred.givenNames === Seq("K.".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "K. T. Frog".opt)
  }


  test("Normally formatted complex names are parsed as expected") {
    val inferred = PersonNameWithDerivations("Dr. Kermit T. Frog III, MD, Ph.D.").inferFully
    assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.prefixes ===  Set("Dr.".n))
    assert(inferred.degrees === Set("M.D.".n,"Ph.D.".n))
    assert(inferred.hereditySuffix === "III".opt)
    assert(inferred.bestFullName === "Dr. Kermit T. Frog III, M.D., Ph.D.".opt)
  }



  test("Solid-caps formatted complex names are parsed as expected") {
    val inferred = PersonNameWithDerivations("DR. KERMIT T. FROG III, MD, PHD").inferFully
    assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.prefixes ===  Set("Dr.".n))
    assert(inferred.degrees === Set("M.D.".n,"Ph.D.".n))
    assert(inferred.hereditySuffix === "III".opt)
    assert(inferred.bestFullName === "Dr. Kermit T. Frog III, M.D., Ph.D.".opt)
  }

	test("Names invert without space") {
		                                   assert(PersonNameWithDerivations("Smith,John").inferFully.bestFullName === "John Smith".opt)
	                                   }
	test("Names invert with space") {
		                                assert(PersonNameWithDerivations("Smith, John").inferFully.bestFullName === "John Smith".opt)
	                                }

  test("Names invert with middle initial") {
    assert(PersonNameWithDerivations("Smith, John Q.").inferFully.bestFullName === "John Q. Smith".opt)
  }
	test("Names don't invert with zero commas") {
		                                            assert(PersonNameWithDerivations("John Smith").inferFully.bestFullName ===
		                                                   "John Smith".opt)
	                                            }
	test("Names don't invert with two commas, easy degree") {
		                                           assert(PersonNameWithDerivations("Smith, John, Ph.D.").inferFully.bestFullName ===
		                                                  "John Smith, Ph.D.".opt)
	                                           }

	test("Names don't invert with two commas, hard degree") {
		                                           assert(PersonNameWithDerivations("Smith, John, PhD").inferFully.bestFullName ===
		                                                  "John Smith, Ph.D.".opt)
	                                           }

  test("Names don't invert with two commas, hard degree, caps") {
    assert(PersonNameWithDerivations("SMITH, JOHN, PhD").inferFully.bestFullName ===
      "John Smith, Ph.D.".opt)
  }

  test("Inverted single first initial without period not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J").inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted single first initial with period not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J.").inferFully.firstName ===
      "J.".opt)
  }

  test("Inverted dual first initial without period mashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, JA").inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted dual first initial without period unmashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J A").inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted dual first initial with period mashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J.A.").inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted dual first initial with period unmashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J. A.").inferFully.firstName ===
      "J.".opt)
  }

  test("inferFully keeps names intact") { "Kermit T. Frog"}

	test("Names may match fully") {assertCanonicalCompatible("John Smith", "John Smith")}

  test("Names may match fully with initial") {assertCanonicalCompatible("John Q. Smith", "John Q. Smith")}

	test("Inverted names match") {assertCanonicalCompatible("Smith, John", "John Smith")}

  test("Inverted names match with initial") {assertCanonicalCompatible("Smith, John Q.", "John Q. Smith")}


	test("Last names must match") {assertNotCanonicalCompatible("John Smith", "John Jones")}

	test("Suffixes supported without comma") {assertCanonicalCompatible("John Smith", "John Smith MD")}

	test("Suffixes supported with comma") {assertCanonicalCompatible("John Smith", "John Smith, MD")}

  test("Multiple suffixes supported with multiple commas") {assertCanonicalCompatible("John Smith", "John Smith, MD, PhD")}

  test("Multiple suffixes supported with multiple commas, inverted") {assertCanonicalCompatible("John Smith", "Smith, John, MD, PhD")}

  test("Multiple suffixes supported with single commas") {assertCanonicalCompatible("John Smith", "John Smith, MD PhD")}

  test("Multiple suffixes supported with no commas") {assertCanonicalCompatible("John Smith", "John Smith MD PhD")}

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

  test("Solid-caps names are compatible with normal variant") {assertCanonicalCompatible("KERMIT THE FROG", "Kermit T. Frog")}


//  test("With inference, ames with particles are compatible with names missing particles") {assertInferredCompatible("Jacqueline du Pre", "Jacqueline Pre")}

  // https://github.com/AnaMarjanica/name-compare/blob/master/src/test/scala/hr/element/etb/name_compare/FuzzyStringTest.scala
  //test("With inference, names are compatible with deaccented versions") { assertInferredCompatible("Ana Đurić", "Ana Duric")}
  // actually we can't deal with the funky D unless we switch the transliterator to icu4j
//  test("With inference, names are compatible with deaccented versions") { assertInferredCompatible("Ana Đurić", "Ana Duric")}

}
