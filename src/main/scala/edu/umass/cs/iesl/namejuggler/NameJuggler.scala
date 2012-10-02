package edu.umass.cs.iesl.namejuggler
import io.Source
import  edu.umass.cs.iesl.scalacommons.StringUtils._
import org.apache.commons.lang.StringEscapeUtils

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */
object NameJuggler {

  def main(args: Array[String])
  {
    println(Seq("prefixes","givenNames","nickNamesInQuotes","surNames","hereditySuffix","degrees","preferredFullName").mkString("\t"))
    for (line <- Source.stdin.getLines.map(_.opt).flatten)
    {
      val p = PersonNameWithDerivations(StringEscapeUtils.unescapeHtml(line).n).inferFully.toCanonical
      println(p.fieldsInCanonicalOrder.map(_.mkString(" ")).mkString("\t"))

    }
  }
}

