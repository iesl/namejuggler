package edu.umass.cs.iesl.namejuggler

import edu.umass.cs.iesl.scalacommons.NonemptyString
import edu.umass.cs.iesl.scalacommons.StringUtils.emptyStringToNone
import edu.umass.cs.iesl.scalacommons.StringUtils.enrichString
import scala.MatchError
import com.weiglewilczek.slf4s.Logging
import annotation.tailrec

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
/**
 * This could be a crf...
 * @return
 */
object PersonNameParser extends Logging {

  import PersonNameFormat._

  private val splitFirst = """^(.*?)[ ]+(.*)$""".r
  private val splitLast = """^(.*[^, ])([, ]+)(.*)$""".r

  def stripPrefixes(s: String): (Set[NonemptyString], String) = {
    try {
      val splitFirst(firstToken, remainder) = s.trim
      if (isPrefix(firstToken)) {
        val (p, r) = stripPrefixes(remainder)
        val f: Option[NonemptyString] = firstToken
        (p + f.get, r)
      }
      else (Set.empty, s)
    }
    catch {
      case e: MatchError => (Set.empty, s)
    }
  }

  def stripSuffixes(s: String): (Option[NonemptyString], Set[NonemptyString], String) = {
    try {
      val splitLast(remainder, separator, lastToken) = s.trim
      if (isHereditySuffix(lastToken)) {
        val (h, d, r) = stripSuffixes(remainder)
        val f: Option[NonemptyString] = lastToken
        if (h.nonEmpty) {
          throw new PersonNameParsingException("More than one heredity suffix: " + s)
        }

        (f, d, r)
      }

      // it can be hard to distinguish degrees from middle initials.
      // Is Smith, John RN == John R. N. Smith, or John Smith, RN?
      // we interpret that case as middle initials, but Smith, John, RN as a degree.

      else if (separator.contains(",") && (remainder.contains(",") || isDegree(lastToken))) {
        logger.debug("Found degree in '" + s + "': '" + lastToken + "'")
        val (h, d, r) = stripSuffixes(remainder)
        val f: Option[NonemptyString] = lastToken
        (h, d + f.get, r)
      }
      else if (!separator.contains(",") && !remainder.contains(",") && isDegree(lastToken)) {
        logger.debug("Found degree in '" + s + "': '" + lastToken + "'")
        val (h, d, r) = stripSuffixes(remainder)
        val f: Option[NonemptyString] = lastToken
        (h, d + f.get, r)
      }
      else (None, Set.empty, s)
    }
    catch {
      case e: MatchError => (None, Set.empty, s)
    }
  }

  // Kermit Kalman the Frog, Ph.D., F.R.S.
  // the Frog, Kermit Kalman, Ph.D., F.R.S.
  // the Frog, Ph.D., F.R.S., Kermit Kalman  // ** this never happens?
  // the Frog, Kermit Kalman
  // we are not parsing lists here, but list context might be informative if multiple names have a consistent format:
  // the Frog KK, Grouch O, and Bird B.
  // KK the Frog, O Grouch, and B Bird.
  // is Jones, M.D. => Michael Douglas Jones or Dr. Jeremiah Jones, MD?  Probably the former. But MD Jones, MD is Dr. Michael Douglas Jones, M.D.
  def parseFullName(s: String): PersonName = {
    val (parsedPrefixes: Set[NonemptyString], noPrefixes: String) = stripPrefixes(s)
    val (parsedHereditySuffix: Option[NonemptyString], parsedDegrees: Set[NonemptyString], coreNameString: String) = stripSuffixes(noPrefixes)

    val coreToks: Array[Array[String]] = coreNameString.split(",").map(_.split(" ").map(_.trim).filter(_.nonEmpty))

    val coreName: PersonName = {
      if (coreToks.size == 0) {
        // no data
        new PersonName() {}
      }
      else if (coreToks.size == 1) {
        // no commas: Kermit Kalman the Frog

        parseUninvertedCore(coreToks.head)
      }
      else if (coreToks.size == 2) {
        // exactly one comma:
        // the Frog, Kermit Kalman
        new PersonNameWithDerivations {
          override val givenNames: Seq[NonemptyString] = coreToks(1).flatMap(emptyStringToNone)

          // declare a single complete surname for now.  If there are several names, they should get expanded later.
          override val surNames: Set[NonemptyString] = emptyStringToNone(coreToks(0).mkString(" ")).toSet
        }
      }
      else {
        throw new PersonNameParsingException("Multiple commas even after removing all degrees")
      }
    }

    val extraName = new PersonName() {
      override val prefixes: Set[NonemptyString] = parsedPrefixes
      override val hereditySuffix = parsedHereditySuffix
      override val degrees = parsedDegrees
    }

    PersonName.merge(Seq(extraName, coreName))
    /*

     // ** return the person name format actually found.  Maybe recurse to steady state?
     def parseFullName(s: String, expectedFormat: PersonNameFormat): (PersonName, PersonNameFormat) = {
       throw new NotImplementedException("Fancy name processing temporarily disabled")
     }

     */
  }

  class PersonNameParsingException(s: String) extends Exception(s)

  private def parseUninvertedCore(nameTokens: Array[String]): PersonName = {
    // OK this is the hard part.  No commas or other structure, so we have to figure out which part is which.
    // ** assume case sensitive for now to help identify the "prelast" particle
    //nameTokens.filter(_.is)
    // detect nicknames in quotes, etc.
    // ** completely simplistic for now

    val (givenR, sur) = splitOnCondition((s: String) => s.isAllLowerCase)(Nil, nameTokens.toList)



    new PersonName {
      override val surNames: Set[NonemptyString] = emptyStringToNone(sur.mkString(" ")).toSet
      //emptyStringToNone(nameTokens.last).toSet
      override val givenNames: Seq[NonemptyString] = {
        val rawGivenNames = givenR.reverse.flatMap(emptyStringToNone)
        if (rawGivenNames.size == 1 && rawGivenNames(0).s.isAllUpperCase && rawGivenNames(0).s.length < 4) {
          // interpret solid caps as initials
          rawGivenNames(0).s.split("").flatMap(emptyStringToNone)
        }
        else {
          rawGivenNames
        }
      }
    }
  }

  @tailrec
  private def splitOnCondition[T](condition: (T => Boolean))(a: List[T], b: List[T]): (List[T], List[T]) = {
    b match {
      case Nil => (a, b)
      case h :: l if (l.isEmpty || condition(h)) => (a, b)
      case h :: l => splitOnCondition[T](condition)(h :: a, l)
    }
  }

  /*
       /*	def parseFullName(s: String): PersonName =
            {
            throw new NotImplementedException("Fancy name processing temporarily disabled")
            }
        */
     // Kermit Kalman the Frog, Ph.D., F.R.S.
     // the Frog, Kermit Kalman, Ph.D., F.R.S.
     // the Frog, Ph.D., F.R.S., Kermit Kalman  // ** this never happens?
     // the Frog, Kermit Kalman


     // we are not parsing lists here, but list context might be informative if multiple names have a consistent format:
     // the Frog KK, Grouch O, and Bird B.
     // KK the Frog, O Grouch, and B Bird.
     def parseFullName(s: String): PersonName =
       {
       //throw new NotImplementedException("Fancy name processing temporarily disabled")
       // split first on commas, then on spaces
       val toks: Array[Array[String]] = s.split(",").map(_.split(" ").map(_.trim).filter(_.nonEmpty))

       // remove any valid prefixes from the front
       val (prefixName: PersonName, toks2: Array[Array[String]]) = stripPrefixes(toks)

       // remove any valid degrees and hereditySuffixes from the end
       val (degreesName: PersonName, coreToks: Array[Array[String]]) = stripSuffixes(toks)

       val coreName: PersonName =
         {
         if (coreToks.size == 0)
           {
           // no data
           new PersonName()
             {}
           }
         else if (coreToks.size == 1)
           {
           // no commas: Kermit Kalman the Frog Ph.D.

           parseUninvertedFullNoPrefix(coreToks.head)
           }
         else if (coreToks.size == 2)
           {
           // exactly one comma:
           // the Frog, Kermit Kalman
           new PersonNameWithDerivations
             {
             override val givenNames = coreToks(1).mkString(" ")
             override val surNames   = Set(coreToks(0).mkString(" "))
             }
           }
         else
           {
           throw new PersonNameParsingException("Multiple commas even after removing all degrees")
           }
         }

       // merge the prefix, degrees, and core data
       PersonName.merge(Seq(prefixName, degreesName, coreName))
       }

     private def parsePrefix(s: String): Option[String] =
       {
       // BAD
       // ** note we are case-sensitive here, but we ignore periods.  Solid caps will fail, eg. MR. KERMIT FROG.
       // the reason is that MR FRITZ could be Mary Roselda Fritz, bur MR. FRITZ could not.
       val q = s.replace(".", "")
       if (validPrefixes.contains(q)) Some(q) else None
       }

     /**
      * Remove any valid prefixes from the front and collect them into a PersonName.
      */
     private def stripPrefixes(ss: Array[Array[String]]): (PersonName, Array[Array[String]]) =
       {
       val p = ss.head.head
       val pp = parsePrefix(p)
       pp match
       {
         case None => (new PersonName()
           {}, ss)
         case Some(s: String) => (new PersonName()
           {prefix = s}, ss.head.tail +: ss.tail)
       }
       }

     private def parseFirstMiddle(ss: Array[String]): PersonName = new PersonNameWithDerivations
       {givenNames = ss.mkString(" ")}



     */
  /******************************************/
  /****** CRUFT BELOW ***********************/
  /*
       val invertedNamePattern = """^([^,]*),([^,]*)$""".r

       /**
      * if there is exactly one comma in the name, reverse the order, e.g. "lastname, firstname" -> "firstname lastname".  In any other case just
      * return the
      * string as is.  Careful: the prefix, suffix, and degree may be confounded with the inversion, e.g. Dr. Soergel, David, Ph.D.
      *
      * @param s
      */
       def uninvertName(s: String): String =
         {
         assert(!s.isEmpty)
         if (s.count(_ == ',') > 1)
           {
           throw new PersonNameParsingException("Too many commas: " + s)
           }
         val q = try
         {
         val invertedNamePattern(lastname, firstname) = s
         if (lastname != null && firstname != null)
           {
           firstname.trim + " " + lastname.trim
           }
         else s
         }
         catch
         {
         case e: MatchError => s
         }
         val r = q.replace("  ", " ").trim
         assert(!r.isEmpty)
         r
         }*/
}

/*case class Person(firstNameInitial: Option[Char] = None, // used only for J. Harrison Ford
				  firstName: Option[String] = None, // preferred name goes here too, e.g. Harrison
				  middleName: Option[String] = None, // often just middle initial // combine as "givenNames"?
				  givenInitials: Option[Seq[Char]] = None, //
				  lastName: Option[String] = None, //
				  pedigree: Option[String] = None, //
				  degree: Option[String] = None, //
				  // droppingParticle: Option[String] = None, //
				  nonDroppingParticle: Option[String] = None, //
				  address: Option[Address] = None, //
				  email: Option[String] = None, //
				  phone: Option[String] = None, //
				  affiliations: Seq[Institution] = Nil, //
				  homepages: Seq[URL] = Nil) //
  {
  // val droppingParticles = Map(("de la" ->("de", "la")))  // ignore for now
  val particles = List("st", "de", "la", "de la", "du", "des", "del", "di", "van", "van den", "von", "zu", "der", "ter")  // "ben"?  "Van't"
  val authorSplit = "(\\S+ )?(.*? )?((" + particles.mkString("|") + ") )?(\\S+)".r
  def Person(x: String)
	{
	val authorSplit(f: String, m: String, p: String, l: String) = x
	val solidCaps = (x == x.toUpperCase)

	f.length match
	{
	  case 0 => // Prufrock
		{
		assert(m.isEmpty)
		}
	  case 1 if m.length > 1 => // J. Alfred X. Prufrock
		{
		firstName = Some(f); // we have only the initial, but call it the "name"
		middleName = Some(m);
		givenInitials = Some(f.head :: m.split(" .").map(_.head).toList)
		}
	  case 2 =>
		{
		if (solidCaps)
		  {
		  if (m.isEmpty) // JA PRUFROCK.  ED GREEN is interpreted as E.D. Green, not Ed Green.
			{
			givenInitials = Some(f.toCharArray.toSeq)
			}
		  else // AL J PRUFROCK interpreted as Al J. Prufrock
			{
			firstName = Some(f);
			middleName = Some(m);
			givenInitials = Some(f.head :: m.split(" .").map(_.head).toList)
			}
		  }
		}
	  case _ => firstName = Some(f);
	}
  }
*/
/*
object PersonNameUtils
	{

	/**
	 * Replace periods with spaces.
	 * if there is exactly one comma in the name, reverse the order, e.g. "lastname, firstname" -> "firstname lastname".  In any other case just return the
	 * string as is.
	 */
	/*	def cleanupNameNoPeriods(s: String): String =
	   {
	   assert(!s.isEmpty)
	   val r = cleanupName(s).replace(".", " ").replace("  ", " ").trim
	   assert(!r.isEmpty)
	   r
	   }

   /**
	* could two names conceivably refer to the same person?  Largely for use within a single WOS record, not for coref
	*/
   def compatibleName(oa: Option[PersonName], ob: Option[PersonName]): Boolean =
	   {
	   // the usual case is that the last names match, but the first name may be an initial
	   // but there may be additional stuff with prefixes, suffixes, middle names, etc. etc.
	   // or just initials
	   // we don't want to do coref here!  Just look for contradictions
	   // two names are compatible iff
	   // a) any string in one name longer than 3 chars is matched in the other string either exactly or by first initial
	   // b) any s

	   (oa, ob) match
	   {
		   case (Some(a), Some(b)) =>
			   {
			   compatibleName(a, b)
			   }
		   case default => false
	   }
	   }

   def compatibleName(a: PersonName, b: PersonName): Boolean =
	   {
	   val aToks = cleanupNameNoPeriods(a).toLowerCase.split(" ").reverse.map(_.trim).filterNot(_.isEmpty)
	   val bToks = cleanupNameNoPeriods(b).toLowerCase.split(" ").reverse.map(_.trim).filterNot(_.isEmpty)
	   if (aToks.isEmpty || bToks.isEmpty)
		   {
		   false
		   }
	   else
		   {
		   val try1 = compatibleTokens(aToks, bToks)

		   val try2 = if (try1) true
		   else
			   {
			   // special case: see if separating a two- or three-character name into initials helps
			   val aToks2: Array[String] = aToks
										   .flatMap(tok => (if (tok.length == 2 || tok.length == 3) tok.toCharArray.reverse.map(_.toString) else Some(tok)))
			   val bToks2: Array[String] = bToks.flatMap(tok => if (tok.length == 2 || tok.length == 3) tok.toCharArray.reverse.map(_.toString)
			   else Some(tok))

			   compatibleTokens(aToks2, bToks2)
			   }

		   try2
		   }
	   }

   private def compatibleTokens(aToks: Array[String], bToks: Array[String]): Boolean =
	   {
	   // basically a simple alignment.  don't bother with DP, just a couple heuristics for the common cases
	   // don't support suffixes; the last names must match, (allowing initials)
	   // suffixes after a comma may be OK due to uninvert
	   val headA = aToks.head
	   val headB = bToks.head
	   if (!((headA equals headB) || (headA equals headB(0).toString) || (headB equals headA(0).toString)))
		   {
		   false
		   }
	   else
		   {
		   // choose the minimum set of first & middle names & initials
		   if (aToks.length <= bToks.length)
			   {
			   compatibleFirstMiddle(aToks.tail, bToks.tail)
			   }
		   else
			   {
			   compatibleFirstMiddle(bToks.tail, aToks.tail)
			   }
		   }
	   }

   // remember tokens are reversed
   private def compatibleFirstMiddle(fewerToks: Array[String], moreToks: Array[String]): Boolean =
	   {

	   if (fewerToks.isEmpty || moreToks.isEmpty) true
	   else
		   {
		   val headX = fewerToks.head
		   val headY = moreToks.head
		   if (!((headX equals headY) || (headX equals headY(0).toString) || (headY equals headX(0).toString)))
			   {
			   // mismatch in first token; try to drop middle name/initial
			   // note this means that "A J Smith" and "J Smith" are compatible; oh well
			   if (fewerToks.length < moreToks.length)
				   compatibleFirstMiddle(fewerToks, moreToks.tail)
			   else false
			   }
		   else
			   {
			   //first initial equal; proceed
			   compatibleFirstMiddle(fewerToks.tail, moreToks.tail)
			   }
		   }
	   }
	   */
	}
*/
