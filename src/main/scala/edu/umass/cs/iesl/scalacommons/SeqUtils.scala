package edu.umass.cs.iesl.scalacommons

import com.typesafe.scalalogging.{StrictLogging => Logging}
import collection.{Set, Iterable, Traversable}
import annotation.tailrec

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
object SeqUtils extends Logging {

  implicit def emptyCollectionToNone[T <: Traversable[Any]](s: T): Option[T] = if (s.isEmpty) None else Some(s)

  def mergeWarn[T, This <: Traversable[T]](a: This, b: This): This = {
    (a, b) match {
      case (Nil, Nil) => a
      case (p, Nil) => a
      case (Nil, q) => b
      case (p, q) => {
        if (p != q) {
          logger.warn("Merging unequal sequences, preferring: " + p + "  to " + q)
        }
        a
      }
    }
  }

  def mergeOrFail[T, This <: Traversable[T]](a: This, b: This): This = {
    (a, b) match {
      case (Nil, Nil) => a
      case (p, Nil) => a
      case (Nil, q) => b
      case (p, q) => {
        if (p != q) {
          throw new SeqMergeException(p, q)
        }
        a
      }
    }
  }

  // based on Daniel Sobral.  http://stackoverflow.com/questions/3050557/how-can-i-extend-scala-collections-with-an-argmax-method
  // but that had serious issues!  Rewritten...

  def argMax[A, B: Ordering](input: Iterable[A], f: A => B): Set[A] = argMaxZip(input, f) map (_._1) toSet

  def argMaxZip[A, B: Ordering](input: Iterable[A], f: A => B): Iterable[(A, B)] = {
    if (input.isEmpty) Nil
    else {
      val fPairs = input map (x => (x, f(x)))
      val maxF = fPairs.map(_._2).max
      fPairs filter (_._2 == maxF)
    }
  }

  // trouble using Ordering.reverse, so just cut and paste for now

  def argMin[A, B: Ordering](input: Iterable[A], f: A => B): Set[A] = argMinZip(input, f) map (_._1) toSet


  def argMinZip[A, B: Ordering](input: Iterable[A], f: A => B): Iterable[(A, B)] = {
    if (input.isEmpty) Nil
    else {
      val fPairs = input map (x => (x, f(x)))
      val minF = fPairs.map(_._2).min
      fPairs filter (_._2 == minF)
    }
  }


  // could do a version that produces (B, Iterable[A]]


  //def filterFoldLeft[B,A](accum: B)(x:A)(op: (B,A) => (B,A)): B = {

  /**
   * A generalization of foldLeft which allows removing more elements from the input than just the head (indeed, which
   * allows non-sequential inputs).  The idea is that the op function filters the input, adds some representation of
   * the removed items to the accumulator, and also returns the remainder.  This process recurses to steady state.
   *
   * @param accum An object representing results accumulated so far
   * @param x An object to filter
   * @param op A function that (in some sense) removes some contents from x and (in some sense) adds something to accum.
   * @tparam B The type of the accumulator
   * @tparam A The type of the input
   * @return
   */
  @tailrec
  def filterFoldLeft[B, A](accum: B, x: A, op: (B, A) => (B, A)): B = {
    val (b, a) = op(accum, x)
    if (b == accum) b // steady state
    else {
      filterFoldLeft[B, A](b, a, op)
    }
  }


  /**
   * Demonstrate that the usual foldLeft is a special case of filterFoldLeft
   * (with the wrinkle that TraversibleOnce.foldLeft is a member function, but here we have to pass in the input;
   * also this is easier to
   */

  def normalFoldLeft[B, A](z: B)(input: Traversable[A])(op: (B, A) => B): B = {
    {
      def f(bb: B, in: Traversable[A]): (B, Traversable[A]) = (op(z, in.head), in.tail)
      filterFoldLeft(z, input, f)
    }
  }
}

class SeqMergeException[T](x: T, y: T) extends Exception("unequal sequences: " + x + "  ,  " + y)
