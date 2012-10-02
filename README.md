Parsing and compatibility testing of person names.

Name Parsing
============

NameJuggler attempts to parse individual names into constituent components.

We represent names as a set of canonical atomic fields, including degrees, nicknames, suffixes, etc.  Being really comprehensive and accurate about this is not possible due to too many
cultural variations and ambiguities.  Still, this should cover most of the cases we care about re authorship of journal articles.

Examples of names that can be parsed:

    Kermit T. Frog
    K. T. Frog
    K.T. Frog
    KT Frog
    Dr. Kermit T. Frog III, MD, Ph.D.
    DR. KERMIT T. FROG III, MD, PHD
    Smith,John
    Smith, John
    Smith, John Q.
    John Smith
    Smith, John, Ph.D.
    Smith, John, PhD
    SMITH, JOHN, PhD
    Smith, J
    Smith, J.
    Smith, JA
    Smith, J A
    Smith, J.A.
    Smith, J. A.
    Frenkel ter Hofstede
    Frenkel Ter Hofstede
    Frenkel de La Silva
    Frenkel la Silva del Ruiz

Examples of names that are __not__ yet correctly parsed:

    Frog III, MD, Ph.D., Dr. Kermit T.
    Frog III, Dr. Kermit T., MD, Ph.D.

Name Compatibility
==================

NameJuggler also compares names for compatibility, i.e. to determine whether two strings could possibly be alternative representations of the same name.  Of course there is no guarantee that two similar names actually represent the same person; but it
s easier to tell whether two names probably do not represent the same person.  So the compatibility tester should be thought of as filter that identifies clearly different names, but makes no claims about the positive cases.  In the simplest case, "A.B." and "AB" are compatible, but many people have those initials.

Some example name pairs that NameJuggler identifies as __incompatible__ include:

      John Smith | John Jones
      John Smith | Jane Smith
    John A Smith | John B Smith
        JA Smith | JB Smith
Edward O. Wilson | EQ Wilson

Some example name pairs that NameJuggler identifies as __compatible__ include:


       John Smith | John Smith
       John Smith | Smith
    John Q. Smith | John Q. Smith
      Smith, John | John Smith
   Smith, John Q. | John Q. Smith
       John Smith | John Smith MD
       John Smith | John Smith, MD
       John Smith | John Smith, MD, PhD
       John Smith | Smith, John, MD, PhD
       John Smith | John Smith, MD PhD
       John Smith | John Smith MD PhD
       John Smith | J Smith
      Smith, John | J Smith
       John Smith | John Archibald Smith
       John Smith | John A Smith
       John Smith | J Archibald Smith
       John Smith | J A Smith
       John Smith | JA Smith
          J Smith | JA Smith
       E O Wilson | Ed Wilson
       E O Wilson | EO Wilson
       E O Wilson | ED O Wilson
       E O Wilson | EDWARD Wilson
 Edward O. Wilson | EO Wilson
Jacqueline du Pre | Jacqueline Pre
        Ana Durić | Ana Duric
Jacqueline du Pré | Jacqueline Pre
  KERMIT THE FROG | Kermit T. Frog



Background
==========

A name is not a fixed thing; it is a probabilistic cloud of strings, all denoting the same person.  Here we don't cover the case that a person changes
names completely; in that case there are two disjoint clouds of strings, so that should be modeled by allowing a person to have multiple PersonNames.

Here we try to model different representations of "the same name".  Variations may include: omitting some components; using initials for some components;
reordering; etc.  The most "different" case to model is that of married names vs. maiden names.  Since one or both of these may appear,
but the other name components are not affected, we consider this a case of multiple surnames within one name.


Things that can be done in code
===============================

NameJuggler is mainly meant to be used as a library.  The simplest way to parse a name is:

    val name : PersonNameWithDerivations = PersonNameWithDerivations("John Q. Public").inferFully

Canonical fields can then be reassembled to produce alternate representations of the name (e.g., representing given names only by initials, etc.)

    assert(name.initials === "J. Q. P.")

and so forth.

It is possible to take multiple name variants as input and coordinate them into a single record.  For instance,
if we assert that Amanda Jones and A. Jones-Archer are the same person, then we should later recognize Amanda Archer as a valid variant.  This is accomplshed by parsing the two
input records and merging them; the result is a PersonName that can match various combinations of the fields from both inputs.


