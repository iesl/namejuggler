package edu.umass.cs.iesl.namejuggler

import com.weiglewilczek.slf4s.Logging


object PersonNameFormat extends Logging {
	// http://notes.ericwillis.com/2009/11/common-name-prefixes-titles-and-honorifics/
	// ** add map from expanded versions, e.g. Professor, Senator, etc.
	// ** Add all manner of religious prefixes, e.g. "very rev", "right rev", "rt. rev", "rt. rev dom", etc.
	private val validPrefixes =
		Seq("Ms", "Miss", "Mrs", "Mr", "Master", "Rev", "Fr", "Dr", "Atty", "Prof", "Hon", "Pres", "Gov", "Coach", "Ofc", "Msgr", "Sr", "Br", "Supt", "Rep",
		    "Sen", "Amb", "Treas", "Sec", "Pvt", "Cpl", "Sgt", "Adm", "Maj", "Capt", "Cmdr", "Lt", "Lt Col", "Col", "Gen")

	// ** don't bother listing these-- too many possibilities
	// http://en.wikipedia.org/wiki/List_of_post-nominal_letters
	//private val validDegrees =
	//	Seq("M.D.","Ph.D")
	private val validHereditySuffixes =
		Seq("Jr.", "Sr.", "II", "III", "IV")

	def isPrefix(s: String): Boolean = {
		s.trim.nonEmpty && (validPrefixes.contains(s))
	}

	def isHereditySuffix(s: String): Boolean = {
		s.trim.nonEmpty && (validPrefixes.contains(s))
	}

	def isDegree(s: String): Boolean = {
		// ** simplistic
		import edu.umass.cs.iesl.scalacommons.StringUtils.enrichString
		val result = s.nonEmpty && (s.filter(_ == '.').nonEmpty || s.isAllUpperCase)
		//logger.debug("Checking Degree:" + s + " : " + s.nonEmpty + " && ( " + s.filter(_ == '.').nonEmpty + " || " + isAllCaps(s) + ")")
		result
	}

}

/**
 * A name format specification, for use both in formatting outputs and for forming expectations when parsing inputs.
 * @param withPrefixes
 * @param withSuffixes
 * @param givenFormat
 * @param surFormat
 * @param inverted
 * @param initialTerminator
 * @param initialSeparator
 * @param degreeAbbreviator
 * @param degreeSeparator
 */
case class PersonNameFormat(withPrefixes: Boolean, givenFormat: NameComponentFormat, surFormat: NameComponentFormat, inverted: Boolean,
                            invertedSeparator: String = ",", withSuffixes: Boolean, initialTerminator: String = ".", initialSeparator: String = ".",
                            degreeAbbreviator: String = ".", degreeSeparator: String = ", ", allCaps: Boolean = false)

sealed class NameComponentFormat

case object Omit extends NameComponentFormat

case object Ambiguous extends NameComponentFormat

case object FirstInitial extends NameComponentFormat

case object AllInitials extends NameComponentFormat

case object OneName extends NameComponentFormat

case object OneNameCaps extends NameComponentFormat

case object AllNames extends NameComponentFormat

case object AllNamesCaps extends NameComponentFormat
