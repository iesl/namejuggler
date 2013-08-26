package edu.umass.cs.iesl.namejuggler

import com.typesafe.scalalogging.slf4j.Logging
import org.scalatest.{FunSuite, BeforeAndAfter}
import edu.umass.cs.iesl.scalacommons.{NonemptyString, StringUtils}


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
class PersonNameTest extends FunSuite with BeforeAndAfter with Logging {

  import StringUtils._

  private def canonicalCompatible(a: String, b: String) : Boolean = {
    val acan = PersonNameWithDerivations(a.n).toCanonical
    val bcan = PersonNameWithDerivations(b.n).toCanonical
    val result = acan compatibleWith bcan
    if (!result) {
      logger.error(acan + " not compatible with " + bcan)
    }
    result
  }

  /*
  private def assertInferredCompatible(a: String, b: String) {
    assert(PersonNameWithDerivations(a).inferFully compatibleWith PersonNameWithDerivations(b).inferFully)
  }*/

  private def notCanonicalCompatible(a: String, b: String) : Boolean = {
    val acan = PersonNameWithDerivations(a.n).toCanonical
    val bcan = PersonNameWithDerivations(b.n).toCanonical
    val result = acan compatibleWith bcan
    if (result) {
      logger.error(acan + " erroneously compatible with " + bcan)
    }
    !result
  }

  test("Normally formatted simple names are parsed as expected") {
    val inferred = PersonNameWithDerivations("Kermit T. Frog".n).inferFully
    assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "Kermit T. Frog".opt)
  }

  test("Initial-formatted simple names with periods and spaces are parsed as expected") {
    val inferred = PersonNameWithDerivations("K. T. Frog".n).inferFully
    assert(inferred.givenNames === Seq("K.".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "K. T. Frog".opt)
  }

  test("Initial-formatted simple names with periods and no spaces are parsed as expected") {
    val inferred = PersonNameWithDerivations("K.T. Frog".n).inferFully
    assert(inferred.givenNames === Seq("K.".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "K. T. Frog".opt)
  }

  test("Initial-formatted simple names without periods are parsed as expected") {
    val inferred = PersonNameWithDerivations("KT Frog".n).inferFully
    assert(inferred.givenNames === Seq("K.".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.bestFullName === "K. T. Frog".opt)
  }


  test("Normally formatted complex names are parsed as expected") {
    val inferred = PersonNameWithDerivations("Dr. Kermit T. Frog III, MD, Ph.D.".n).inferFully
    assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.prefixes === Set("Dr.".n))
    assert(inferred.degrees === Set("M.D.".n, "Ph.D.".n))
    assert(inferred.hereditySuffix === "III".opt)
    assert(inferred.bestFullName === "Dr. Kermit T. Frog III, M.D., Ph.D.".opt)
  }

  test("Inverted complex names with internal degrees are parsed as expected") {
    pendingUntilFixed {
      val inferred = PersonNameWithDerivations("Frog III, MD, Ph.D., Dr. Kermit T.".n).inferFully
      assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
      assert(inferred.surNames === Set("Frog".n))
      assert(inferred.allInitials === "K. T. F.".opt)
      assert(inferred.prefixes === Set("Dr.".n))
      assert(inferred.degrees === Set("M.D.".n, "Ph.D.".n))
      assert(inferred.hereditySuffix === "III".opt)
      assert(inferred.bestFullName === "Dr. Kermit T. Frog III, M.D., Ph.D.".opt)
    }
  }

  test("Inverted complex names with external degrees are parsed as expected") {
    pendingUntilFixed {
      val inferred = PersonNameWithDerivations("Frog III, Dr. Kermit T., MD, Ph.D.".n).inferFully
      assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
      assert(inferred.surNames === Set("Frog".n))
      assert(inferred.allInitials === "K. T. F.".opt)
      assert(inferred.prefixes === Set("Dr.".n))
      assert(inferred.degrees === Set("M.D.".n, "Ph.D.".n))
      assert(inferred.hereditySuffix === "III".opt)
      assert(inferred.bestFullName === "Dr. Kermit T. Frog III, M.D., Ph.D.".opt)
    }
  }



  test("Solid-caps formatted complex names are parsed as expected") {
    val inferred = PersonNameWithDerivations("DR. KERMIT T. FROG III, MD, PHD".n).inferFully
    assert(inferred.givenNames === Seq("Kermit".n, "T.".n))
    assert(inferred.surNames === Set("Frog".n))
    assert(inferred.allInitials === "K. T. F.".opt)
    assert(inferred.prefixes === Set("Dr.".n))
    assert(inferred.degrees === Set("M.D.".n, "Ph.D.".n))
    assert(inferred.hereditySuffix === "III".opt)
    assert(inferred.bestFullName === "Dr. Kermit T. Frog III, M.D., Ph.D.".opt)
  }

  test("Names invert without space") {
    assert(PersonNameWithDerivations("Smith,John".n).inferFully.bestFullName === "John Smith".opt)
  }
  test("Names invert with space") {
    assert(PersonNameWithDerivations("Smith, John".n).inferFully.bestFullName === "John Smith".opt)
  }

  test("Names invert with middle initial") {
    assert(PersonNameWithDerivations("Smith, John Q.".n).inferFully.bestFullName === "John Q. Smith".opt)
  }
  test("Names don't invert with zero commas") {
    assert(PersonNameWithDerivations("John Smith".n).inferFully.bestFullName ===
      "John Smith".opt)
  }
  test("Names don't invert with two commas, easy degree") {
    assert(PersonNameWithDerivations("Smith, John, Ph.D.".n).inferFully.bestFullName ===
      "John Smith, Ph.D.".opt)
  }

  test("Names don't invert with two commas, hard degree") {
    assert(PersonNameWithDerivations("Smith, John, PhD".n).inferFully.bestFullName ===
      "John Smith, Ph.D.".opt)
  }

  test("Names don't invert with two commas, hard degree, caps") {
    assert(PersonNameWithDerivations("SMITH, JOHN, PhD".n).inferFully.bestFullName ===
      "John Smith, Ph.D.".opt)
  }

  test("Inverted single first initial without period not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J".n).inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted single first initial with period not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J.".n).inferFully.firstName ===
      "J.".opt)
  }

  test("Inverted dual first initial without period mashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, JA".n).inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted dual first initial without period unmashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J A".n).inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted dual first initial with period mashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J.A.".n).inferFully.firstName ===
      "J.".opt)
  }
  test("Inverted dual first initial with period unmashed not interpreted as degree") {
    assert(PersonNameWithDerivations("Smith, J. A.".n).inferFully.firstName ===
      "J.".opt)
  }

  test("Initials with periods unmashed interpreted as initials") {
    val n = PersonNameWithDerivations("J. A. S.".n).inferFully
    assert(n.firstName === "J.".opt)
    assert(n.givenInitials === "J. A.".opt)
    assert(n.surNames.contains("S.".n))
  }

  test("Initials with periods mashed interpreted as initials") {
    val n = PersonNameWithDerivations("J.A.S.".n).inferFully
    assert(n.firstName === "J.".opt)
    assert(n.givenInitials === "J. A.".opt)
    assert(n.surNames.contains("S.".n))
  }

  test("Initials without periods mashed interpreted as initials") {
    val n = PersonNameWithDerivations("JAS".n).inferFully
    assert(n.firstName === "J.".opt)
    assert(n.givenInitials === "J. A.".opt)
    assert(n.surNames.contains("S.".n))
  }

  test("Initials without periods unmashed interpreted as initials") {
    val n = PersonNameWithDerivations("J A S".n).inferFully
    assert(n.firstName === "J.".opt)
    assert(n.givenInitials === "J. A.".opt)
    assert(n.allInitials === "J. A. S.".opt)
    assert(n.surNames.contains("S".n))
  }

  test("Particle preceding surname is detected if lowercase") {
    val sur = PersonNameWithDerivations("Frenkel ter Hofstede".n).inferFully.surNames
    assert(sur.contains("ter Hofstede".n))
    //assert(sur.contains("Hofstede".n))  // "Frenkel Hofstede" will still pass the compatibility test though, see below
  }
  test("Particle preceding surname is detected if capitalized") {
    val sur = PersonNameWithDerivations("Frenkel Ter Hofstede".n).inferFully.surNames
    assert(sur.contains("ter Hofstede".n))
    //assert(sur.contains("Hofstede".n))  // "Frenkel Hofstede" will still pass the compatibility test though, see below
  }
  test("Multiple particles preceding surnames are detected even with mixed case") {
    val sur = PersonNameWithDerivations("Frenkel de La Silva".n).inferFully.surNames
    assert(sur.contains("de la Silva".n))
    //assert(sur.contains("la Silva".n))
    //assert(sur.contains("Silva".n))
  }
  test("Multiple surnames with particles are detected") {
    val sur = PersonNameWithDerivations("Frenkel la Silva del Ruiz".n).inferFully.surNames
    assert(sur.contains("la Silva del Ruiz".n))
    //assert(sur.contains("la Silva".n))
    //assert(sur.contains("del Ruiz".n))
    //assert(sur.contains("Silva".n))
    //assert(sur.contains("Ruiz".n))
  }

  test("inferFully keeps names intact") {
    assert(PersonNameWithDerivations("Kermit T. Frog".n).inferFully.bestFullName.get.s === "Kermit T. Frog")
  }

  test("Names may match fully") {
    assert(canonicalCompatible("John Smith", "John Smith"))
  }
  test("Last name alone matches") {
    assert(canonicalCompatible("John Smith", "Smith"))
  }
  test("First name alone does not match") {
    assert(notCanonicalCompatible("John Smith", "John"))
  }

  test("Names may match fully with initial") {
    assert(canonicalCompatible("John Q. Smith", "John Q. Smith"))
  }

  test("Inverted names match") {
    assert(canonicalCompatible("Smith, John", "John Smith"))
  }

  test("Inverted names match with initial") {
    assert(canonicalCompatible("Smith, John Q.", "John Q. Smith"))
  }

  test("Names may match against two initials joined") {
    assert(canonicalCompatible("John Smith", "JS"))
  }
  test("Names may match against two initials with periods") {
    assert(canonicalCompatible("John Smith", "J.S."))
  }
  test("Names may match against two initials with spaces") {
    assert(canonicalCompatible("John Smith", "J S"))
  }
  test("Names may match against two initials with periods and spaces") {
    assert(canonicalCompatible("John Smith", "J. S."))
  }

  test("Names may match against three initials joined") {
    assert(canonicalCompatible("John Smith", "JQS"))
  }

  test("Initials match with and without periods") {
    assert(canonicalCompatible("JS", "J.S."))
  }

  test("Initials match with and without spaces") {
    assert(canonicalCompatible("JS", "J S"))
  }

  test("Middle initials must match") {
    assert(notCanonicalCompatible("JQS", "JPS"))
  }

  test("Last names must match") {
    assert(notCanonicalCompatible("John Smith", "John Jones"))
  }

  test("Suffixes supported without comma") {
    assert(canonicalCompatible("John Smith", "John Smith MD"))
  }

  test("Suffixes supported with comma") {
    assert(canonicalCompatible("John Smith", "John Smith, MD"))
  }

  test("Multiple suffixes supported with multiple commas") {
    assert(canonicalCompatible("John Smith", "John Smith, MD, PhD"))
  }

  test("Multiple suffixes supported with multiple commas, inverted") {
    assert(canonicalCompatible("John Smith", "Smith, John, MD, PhD"))
  }

  test("Multiple suffixes supported with single commas") {
    assert(canonicalCompatible("John Smith", "John Smith, MD PhD"))
  }

  test("Multiple suffixes supported with no commas") {
    assert(canonicalCompatible("John Smith", "John Smith MD PhD"))
  }

  test("First names must match") {
    assert(notCanonicalCompatible("John Smith", "Jane Smith"))
  }

  test("First names may match as initial") {
    assert(canonicalCompatible("John Smith", "J Smith"))
  }

  test("First names may match as initial, inverted") {
    assert(canonicalCompatible("Smith, John", "J Smith"))
  }

  test("Adding middle name OK") {
    assert(canonicalCompatible("John Smith", "John Archibald Smith"))
  }

  test("Adding middle initial OK") {
    assert(canonicalCompatible("John Smith", "John A Smith"))
  }

  test("Adding middle name OK with first initial") {
    assert(canonicalCompatible("John Smith", "J Archibald Smith"))
  }

  test("Adding middle initial OK with first initial") {
    assert(canonicalCompatible("John Smith", "J A Smith"))
  }

  test("Wrong middle initials are incompatible") {
    assert(notCanonicalCompatible("John A Smith", "John B Smith"))
  }

  test("Wrong first initials are incompatible given matching middle") {
    assert(notCanonicalCompatible("A John Smith", "B John Smith"))
  }

  test("Wrong first names are incompatible given matching middle") {
    assert(notCanonicalCompatible("Alberforth John Smith", "Bartholemew John Smith"))
  }

  test("Adding joined middle initial OK with first initial") {
    assert(canonicalCompatible("John Smith", "JA Smith"))
  }

  test("Initial vs joined middle initial are compatible") {
    assert(canonicalCompatible("J Smith", "JA Smith"))
  }

  test("Wrong joined middle initials are incompatible") {
    assert(notCanonicalCompatible("JA Smith", "JB Smith"))
  }
  test("Wrong joined first initials are incompatible") {
    assert(notCanonicalCompatible("AJ Smith", "BJ Smith"))
  }

  test("Short first name interpreted as name, not initials, when not solid caps") {
    assert(canonicalCompatible("E O Wilson", "Ed Wilson"))
  }

  test("Solid caps short first name interpreted as initials") {
    assert(canonicalCompatible("E O Wilson", "EO Wilson"))
  }

  test("Solid caps short first name not interpreted as initials when there are other initals") {
    assert(canonicalCompatible("E O Wilson", "ED O Wilson"))
  }

  test("Solid caps long first name interpreted as name") {
    assert(canonicalCompatible("E O Wilson", "EDWARD Wilson"))
  }

  test("Correct joined middle initial is compatible with full name") {
    assert(canonicalCompatible("Edward O. Wilson", "EO Wilson"))
  }

  test("Wrong joined middle initial is incompatible with full name") {
    assert(notCanonicalCompatible("Edward O. Wilson", "EQ Wilson"))
  }

  test("Lower-case middle names are interpreted as particles") {
    assert(PersonNameWithDerivations("Jacqueline du Pre".n).toCanonical.surNames.contains(NonemptyString("du Pre")))
  }

  test("Names with particles are compatible with names missing particles") {
    assert(canonicalCompatible("Jacqueline du Pre", "Jacqueline Pre"))
  }

  // https://github.com/AnaMarjanica/name-compare/blob/master/src/test/scala/hr/element/etb/name_compare/FuzzyStringTest.scala
  test("Names are compatible with deaccented versions") {
    assert(canonicalCompatible("Ana Durić", "Ana Duric"))
  }

  test("Names with particles and accents are compatible with simplified variant") {
    assert(canonicalCompatible("Jacqueline du Pré", "Jacqueline Pre"))
  }

  test("Solid-caps names are compatible with normal variant") {
    assert(canonicalCompatible("KERMIT THE FROG", "Kermit T. Frog"))
  }

  test("Hyphenated first name compatible with different middle initial") {
    assert(canonicalCompatible("Peggy-Sue Smith", "PQ Smith"))
  }

  test("Last initial of name starting with Mc, Mac, or O etc. is just M or O respectively") {
    assert(canonicalCompatible("Padraic O'Brian", "PO"))
    assert(canonicalCompatible("Padraic MacDonald", "PM"))
  }
  test("Root initial of name starting with Mc, Mac, or O etc. is not recognized") {
    assert(notCanonicalCompatible("Padraic O'Brian", "PB"))
    assert(notCanonicalCompatible("Padraic MacDonald", "PD"))
  }

  test("Extended-form initials of name starting with Mc, Mac, or O etc. are not recognized") {
    assert(notCanonicalCompatible("Padraic O'Brian", "P O'B"))
    assert(notCanonicalCompatible("Padraic MacDonald", "P MacD"))
  }


  //  test("With inference, ames with particles are compatible with names missing particles") {assertInferredCompatible("Jacqueline du Pre", "Jacqueline Pre")}

  // https://github.com/AnaMarjanica/name-compare/blob/master/src/test/scala/hr/element/etb/name_compare/FuzzyStringTest.scala
  //test("With inference, names are compatible with deaccented versions") { assertInferredCompatible("Ana Đurić", "Ana Duric")}
  // actually we can't deal with the funky D unless we switch the transliterator to icu4j
  //  test("With inference, names are compatible with deaccented versions") { assertInferredCompatible("Ana Đurić", "Ana Duric")}

}
