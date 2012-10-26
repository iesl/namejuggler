package edu.umass.cs.iesl.namejuggler

import io.Source
import edu.umass.cs.iesl.scalacommons.StringUtils._
import org.apache.commons.lang.StringEscapeUtils
import annotation.tailrec
import collection.mutable
import scala.Predef._
import edu.umass.cs.iesl.scalacommons.SeqUtils
import com.weiglewilczek.slf4s.Logging

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */
object NameJuggler extends Logging {
  def main(args: Array[String]) {
    if (args.find(_.==("--compat")).isDefined) {
      for (line <- Source.stdin.getLines.map(_.opt).flatten) {
        val l = StringEscapeUtils.unescapeHtml(line).n
        val names = l.split("[\t;|]").map(_.trim).filterNot(_.isEmpty)
        logger.info("Testing compability among " + names.size + " names.")
        val z = for (b <- names) yield (b, PersonNameWithDerivations(b.n).toCanonical)
        //val result = NameCliquer.allVsAllCompatibility(z)
        //for (x <- result.keys) println(x + "\t" + result(x).mkString("\t"))

        logger.debug("Testing compability among " + names.size + " parsed names.")
        val (cliques, transitiveEdgeLists) = NameCliquer.findCompatibilityGroups(z)
        for (c <- cliques) println("CLIQUE\t" + c.mkString("\t"))
        //for (c <- transitive) println("TRANS\t" + c.mkString("\t"))

        for (c <- transitiveEdgeLists) {
          println("graph TRANS {")
          for ((a, b) <- c) println(a + " -- " + b + ";")
          println("}")
        }

      }
    }
    else {
      println(Seq("prefixes", "givenNames", "nickNamesInQuotes", "surNames", "hereditySuffix", "degrees", "preferredFullName").mkString("\t"))
      for (line <- Source.stdin.getLines.map(_.opt).flatten) {
        val l = StringEscapeUtils.unescapeHtml(line).n
        val p = PersonNameWithDerivations(l).inferFully.toCanonical
        println(p.fieldsInCanonicalOrder.map(_.mkString(" ")).mkString("\t"))
      }
    }

  }
}

object NameCliquer extends Logging {

  def isClique(nodes: Set[String], adjacency: Map[String, Set[String]]): Boolean =
    nodes.find(n => !((nodes - n) subsetOf adjacency(n))).isEmpty

  def findCompatibilityGroups(namesWithParsed: Seq[(String, CanonicalPersonName)]): (Set[Set[String]], Set[Set[(String, String)]]) = {
    val adjacency = allVsAllCompatibility(namesWithParsed)
    logger.debug("found " + adjacency.size + " pairwise compatibilities among " + namesWithParsed.size + " nodes.")
    val partitions = findPartitions(adjacency)
    logger.debug("found " + partitions.size + " partitions.")
    val (cliques, transitive) = partitions.partition(isClique(_, adjacency))
    val transitiveEdgeLists = for (p <- transitive) yield {
      (for (n <- p) yield {
        adjacency(n) map (b => {
          if (n < b) (n, b) else (b, n)
        })
      }).flatten
    }
    (cliques, transitiveEdgeLists)
  }

  private def findPartitions[A](adjacency: Map[A, Set[A]]): Set[Set[A]] = {
    def f(accum: Set[Set[A]], input: Map[A, Set[A]]): (Set[Set[A]], Map[A, Set[A]]) = {
      input match {
        case x if x.isEmpty => (accum, input) // steady state
        case _ => {
          val (a, b) = getOnePartition(input)
          (accum + a, b)
        }
      }
    }

    SeqUtils.filterFoldLeft(Set.empty[Set[A]], adjacency, f)
  }

  private def getOnePartition[A](adjacency: Map[A, Set[A]]): (Set[A], Map[A, Set[A]]) = {

    val (node, neighbors) = adjacency.head

    val done: mutable.Set[A] = mutable.Set.empty
    val members: mutable.Set[A] = mutable.Set.empty + node

    def collectPartition(focal: A) {
      val neighbors = adjacency(focal)
      for (n <- neighbors) {
        members.add(n)
      }
      done.add(focal)
      for (n <- (neighbors -- done)) collectPartition(n)
    }

    collectPartition(node)
    logger.debug("Found partition with " + members.size + " members")
    (members.toSet, adjacency -- members)
  }

  def allVsAllCompatibility(namesWithParsed: Seq[(String, CanonicalPersonName)]): Map[String, Set[String]] = {

    logger.debug("Testing compability among " + namesWithParsed.size + " names.")

    val accum: mutable.Map[String, mutable.Set[String]] = mutable.Map() ++ (for ((s, c) <- namesWithParsed) yield (s, mutable.Set[String]()))

    allVsAllCompatibility(namesWithParsed, accum)
    val twiceLinks: Int = accum.map(_._2.size).sum
    logger.debug("Done testing compability among " + namesWithParsed.size + " names, found " + twiceLinks)
    val result = accum.mapValues(_.toSet).toMap // make nonmutable

    val twiceLinks2: Int = result.map(_._2.size).sum
    logger.debug("Done testing compability among " + namesWithParsed.size + " names, found " + twiceLinks2)

    result
  }

  @tailrec
  private def allVsAllCompatibility(namesWithParsed: Seq[(String, CanonicalPersonName)], accum: mutable.Map[String, mutable.Set[String]]) {

    logger.debug("Recursive testing compability among " + namesWithParsed.size + " names.")
    namesWithParsed match {
      case Nil =>
      case _ =>

        val (aOrig, aParsed) = namesWithParsed.head
        val rest = namesWithParsed.tail

        // would be nice to parallelize here, but that makes a mess updating the mutable sets.  Needs refactor...
        for ((bOrig, bParsed) <- rest if (aParsed compatibleWith bParsed)) {
          logger.debug("Found compatible pair: " + aOrig + "  |  " + bOrig)
          accum.getOrElseUpdate(aOrig, mutable.Set[String]()) += (bOrig)
          accum.getOrElseUpdate(bOrig, mutable.Set[String]()) += (aOrig)
        }
        allVsAllCompatibility(rest, accum)
    }
  }

}
