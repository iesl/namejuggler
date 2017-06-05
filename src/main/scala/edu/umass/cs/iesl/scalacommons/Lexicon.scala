package edu.umass.cs.iesl.scalacommons

import java.io.InputStream
import StringUtils._
import scala.util.matching.Regex
import com.typesafe.scalalogging.{StrictLogging => Logging}

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

// usage:
// val stopwords = new Lexicon("stopwords")
// or
// val stopwords = new Lexicon(getClass.getResourceAsStream("/lexicons/stopwords"))

class Lexicon(s: InputStream) extends Logging {

  def this(name: String) =
    this({
      val is = getClass.getResourceAsStream("/lexicons/" + name);
      if (is != null) is else getClass.getResourceAsStream("/lexicons/" + name + ".txt")
    })

  private val (lexTokens, lexTokensLC, lexTokensStrippedLCREs) = {
    val text: String = IOUtils.loadText(s)

    val lexTokens: Set[String] = text.split("\n").toSet[String].map(_.trim).filterNot(_.isEmpty).filterNot(_.startsWith("#"))

    for (t <- lexTokens if t.contains(" ")) {
      logger.warn("Token contains a space: " + t + ", use countSubstringMatchesLC")
    }
    val lexTokensLC: Set[String] = lexTokens.map(_.toLowerCase)
    val lexTokensStrippedLCREs: Set[Regex] = lexTokensLC.map(_.stripPunctuation.r)
    (lexTokens, lexTokensLC, lexTokensStrippedLCREs)
  }

  def countMatches(tokens: Seq[String]): Int = {
    tokens.count(lexTokens.contains)
  }

  def countMatchesLC(tokens: Seq[String]): Int = {
    tokens.count(s => lexTokensLC.contains(s.toLowerCase))
  }

  // Maybe: consider newlines in the input to be a hard constraint, but strip the spaces within each line
  // careful, the matches might overlap and double-count tokens etc.
  def countSubstringMatchesLC(s: String): Int = substringMatchesLC(s).values.sum

  def matches(tokens: Seq[String]): Seq[String] = tokens.filter(t => lexTokens.contains(t))

  def matchesLC(tokens: Seq[String]): Seq[String] = tokens.filter(t => lexTokensLC.contains(t.toLowerCase))

  def countTokenMatchesLC(s: String): Int = countMatchesLC(s.split("\\W+"))

  def substringMatchesLC(s: String): Map[String, Int] = {
    val target = s.toLowerCase
    val matchesPerToken = (for (r <- lexTokensStrippedLCREs) yield (r.toString(), r.findAllIn(target).size)).filter(_._2 > 0).toMap
    matchesPerToken
  }

  // use these as filters, e.g. mySeq.filter(someLexicon.exact)

  def exact(s: String): Boolean = lexTokens.contains(s)

  def lc(s: String): Boolean = lexTokensLC.contains(s)


}
