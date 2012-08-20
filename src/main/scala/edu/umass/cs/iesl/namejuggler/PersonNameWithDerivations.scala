package edu.umass.cs.iesl.namejuggler

import edu.umass.cs.iesl.scalacommons.{SeqUtils, StringUtils, OptionUtils}
import StringUtils._
import edu.umass.cs.iesl.scalacommons.NonemptyString

object PersonNameWithDerivations {
  def apply(s: String): PersonNameWithDerivations = {
    new PersonNameWithDerivations {
      override val fullNames = Set(NonemptyString(s))
    }
  }

  /**
   * Make a new PersonNameWithDerivations.  Generally we should prefer the "primary" values, and back off to the secondary values.
   * However sometimes we may judge that the backup value is superior (e.g., full name vs initial).

   * @return
   */
  def merge(primary: PersonNameWithDerivations, secondary: PersonNameWithDerivations): PersonNameWithDerivations = {
    val mergedCanonical = PersonName.merge(primary, secondary)

    new PersonNameWithDerivations {
      // first set the canonical fields
      override lazy val preferredFullName = mergedCanonical.preferredFullName
      override lazy val prefixes = mergedCanonical.prefixes
      override lazy val givenNames = mergedCanonical.givenNames
      override lazy val nickNames = mergedCanonical.nickNames
      override lazy val surNames = mergedCanonical.surNames
      override lazy val hereditySuffix = mergedCanonical.hereditySuffix
      override lazy val degrees = mergedCanonical.degrees

      // then merge the derived fields.  The result may be different from just rederiving them!
      override lazy val firstInitial = OptionUtils.mergeWarn(primary.firstInitial, secondary.firstInitial)
      override lazy val middleInitials = OptionUtils.mergeWarn(primary.middleInitials, secondary.middleInitials)
      override lazy val lastInitial = OptionUtils.mergeWarn(primary.lastInitial, secondary.lastInitial)
      override lazy val givenInitials = OptionUtils.mergeWarn(primary.givenInitials, secondary.givenInitials)
      override lazy val allInitials = OptionUtils.mergeWarn(primary.allInitials, secondary.allInitials)

      override lazy val firstName = OptionUtils.mergeWarn(primary.firstName, secondary.firstName)
      override lazy val middleNames = SeqUtils.mergeWarn(primary.middleNames, secondary.middleNames)

      // various representations of full name may be present in a single mention (e.g., a metadata xml file)
      override lazy val fullNames = primary.fullNames ++ secondary.fullNames
    }
  }
}

trait PersonNameWithDerivations extends PersonName {
  // a person mention may distinguish name components, or not.
  // here, just store whatever we get (i.e. if we get first and last, leave fullname blank).
  // then we can do whatever normalizations are needed later.
  // firstInitial is not just a char; could be Ja. for Jacques, etc.
  // DS, DAWS, D.S., D.A.W.S, D. S., D S, etc.
  def firstInitial: Option[NonemptyString] = None

  def middleInitials: Option[NonemptyString] = None

  // van Dyke = v.D. ?
  def lastInitial: Option[NonemptyString] = None

  // initials may be for full name, or just given names (e.g., from PubMed)
  def givenInitials: Option[NonemptyString] = None

  def allInitials: Option[NonemptyString] = None

  // typically first name + middle initial, etc.
  //def givenNames: Option[NonemptyString] = None
  def firstName: Option[NonemptyString] = None

  def middleNames: Seq[NonemptyString] = Nil

  // various representations of full name may be present in a single mention (e.g., a metadata xml file)
  def fullNames: Set[NonemptyString] = Set.empty

  // assume that the longest is the most informative
  final def longestSurName: Option[NonemptyString] = surNames.toSeq.sortBy(-_.s.size).headOption

  // assume that the longest is the most informative
  final def longestFullName: Option[NonemptyString] = fullNames.toSeq.sortBy(-_.s.size).headOption

  final def bestFullName: Option[NonemptyString] = preferredFullName.orElse(longestFullName)

  /**
   * Propagate info around all the fields.
   * @return
   */
  def inferFully: PersonNameWithDerivations = {
    // first infer canonical fields only from derived fields
    //val proposedCanonical: CanonicalPersonName = new InferredCanonicalPersonName(this)
    // override those with explicit canonical fields
    val canonical = PersonName.merge(this, toCanonical)
    // rederive all fields
    val rederived = canonical.withDerivations
    // override those with explicit derived fields
    val result = PersonNameWithDerivations.merge(this, rederived)
    result
  }

/*
  def compatibleWith(other: PersonNameWithDerivations): Boolean = {
    inferFully.toCanonical compatibleWith( other.inferFully.toCanonical)
  }
*/

  override def toString = bestFullName.map(_.s).getOrElse("")

  def toCanonical: CanonicalPersonName = new InferredCanonicalPersonName(this)

  /*
//.orElse(n.lastInitial)
override val firstName                        =
  {
  var gf: Option[NonemptyString] = n.givenNames.map(r => NonemptyString(r.split(" ").head))
  var asd: Option[NonemptyString] = nParsedFullNames.map(_.firstName).flatten.headOption.map(NonemptyString(_))
  n.firstName.orElse(gf).orElse(asd).orElse(n.firstInitial)
  }
override val middleNames: Seq[NonemptyString] =
  {
  import SeqUtils.emptyCollectionToNone
  val mid: Option[Seq[NonemptyString]] = n.givenNames.map(_.split(" ").tail.filter(_.nonEmpty).map(new NonemptyString(_)))
  val fromInitials: Option[Seq[NonemptyString]] = n.middleInitials.map(_.split(" ").filter(_.nonEmpty).map(new NonemptyString(_)))
  val result: Seq[NonemptyString] =
    emptyCollectionToNone[Seq[NonemptyString]](n.middleNames).orElse(mid).orElse(fromInitials).orElse(Nil).getOrElse(Nil)
  result
  }*/
}

/**
 * Derive all derivable fields solely from provided canonical fields.
 *
 * @param n
 */
class CanonicalPersonNameWithDerivations(n: CanonicalPersonName) extends CanonicalPersonName with PersonNameWithDerivations {
  override def toCanonical = n

  // first copy the canonical fields
  override lazy val prefixes = n.prefixes
  override lazy val givenNames = n.givenNames
  override lazy val nickNames = n.nickNames
  override lazy val surNames = n.surNames
  override lazy val hereditySuffix = n.hereditySuffix
  override lazy val degrees = n.degrees

  // ** lots not implemented and generally broken
  // then derive the remaining fields
  override lazy val firstName: Option[NonemptyString] = n.givenNames.headOption
  override lazy val middleNames: Seq[NonemptyString] = n.givenNames.tail
  override lazy val firstInitial: Option[NonemptyString] = firstName.map(x => NonemptyString(x.s(0) + "."))
  override lazy val middleInitials: Option[NonemptyString] = middleNames.map(_.s(0) + ".").mkString(" ").trim

  // ** We just take the first uppercase letter from the longest surname,
  // desJardins?  drop all particles?  etc. etc.
  // is Amanda Jones-Albrecht => A. J. or A. J.-A. or what?
  // yuck
  //** for now, just take the first capital letter from the longest surname
  override lazy val lastInitial: Option[NonemptyString] = longestSurName.map(x => NonemptyString((("[A-Z]".r findAllIn x.s).toSeq)(0) + "."))
  override lazy val givenInitials: Option[NonemptyString] = List(firstInitial, middleInitials).flatten.mkString(" ").trim
  override lazy val allInitials: Option[NonemptyString] = List(firstInitial, middleInitials, lastInitial).flatten.mkString(" ").trim

  // van Dyke = v.D. ?
  // various representations of full name may be present in a single mention (e.g., a metadata xml file)
  override lazy val preferredFullName: Option[NonemptyString] = {
    val prefixString: Option[NonemptyString] = prefixes.mkString(" ").trim
    val givenString: Option[NonemptyString] = givenNames.mkString(" ").trim
    val degreesString: Option[NonemptyString] = degrees.map(", " + _).mkString(" ").trim
    val rebuiltFullName: Option[NonemptyString] = Seq(prefixString, givenString, nickNamesInQuotes, longestSurName,
      hereditySuffix).flatten.mkString(" ").trim + degreesString.getOrElse("")
    rebuiltFullName
    //if(rebuiltFullName.nonEmpty) Set(rebuiltFullName) else Set.empty
  }
  override lazy val fullNames: Set[NonemptyString] = {
    preferredFullName.toSet
  }
}
