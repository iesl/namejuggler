/*
 * Copyright (c) 2013  University of Massachusetts Amherst
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.umass.cs.iesl.namejuggler

import edu.umass.cs.iesl.scalacommons.Lexicon

object Lexicons {
  val firstnameHigh = new Lexicon(getClass.getResourceAsStream("/lexicons/firstnameHigh"))
  val firstnameHighest = new Lexicon(getClass.getResourceAsStream("/lexicons/firstnameHighest"))
  //val firstnameMed = new Lexicon(getClass.getResourceAsStream("/lexicons/firstnameMed"))
  //val jobtitle = new Lexicon(getClass.getResourceAsStream("/lexicons/jobtitle"))
  //val lastnameHigh = new Lexicon(getClass.getResourceAsStream("/lexicons/lastnameHigh"))
  //val lastnameHighest = new Lexicon(getClass.getResourceAsStream("/lexicons/lastnameHighest"))
  //val lastnameMed = new Lexicon(getClass.getResourceAsStream("/lexicons/lastnameMed"))
}
