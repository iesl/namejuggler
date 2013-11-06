NameJuggler
===========

_Parsing and compatibility testing of person names._

Name Parsing
------------

NameJuggler attempts to parse individual names into constituent components based on simple heuristics.

We represent names as a set of canonical atomic fields, including degrees, nicknames, suffixes, etc.  Being really comprehensive and accurate about this is not possible due to too many
cultural variations and ambiguities.  Still, this should cover most of the cases we care about re authorship of journal articles.

Examples of names that can be parsed:

| input | prefixes | givenNames | nickNamesInQuotes | surNames | hereditySuffix | degrees | preferredFullName |
|-:|-|-|-|-|-|-|-|
Kermit T. Frog |  | Kermit T. |  | Frog |  |  |
K. T. Frog |  | K. T. |  | Frog |  |  |
K.T. Frog |  | K. T. |  | Frog |  |  |
KT Frog |  | K. T. |  | Frog |  |  |
Dr. Kermit T. Frog III, MD, Ph.D. | Dr. | Kermit T. |  | Frog | III | M.D. Ph.D. |
DR. KERMIT T. FROG III, MD, PHD | Dr. | Kermit T. |  | Frog | III | M.D. Ph.D. |
Smith,John |  | John |  | Smith |  |  |
Smith, John |  | John |  | Smith |  |  |
Smith, John Q. |  | John Q. |  | Smith |  |  |
John Smith |  | John |  | Smith |  |  |
Smith, John, Ph.D. |  | John |  | Smith |  | Ph.D. |
Smith, John, PhD |  | John |  | Smith |  | Ph.D. |
SMITH, JOHN, PhD |  | John |  | Smith |  | Ph.D. |
Smith, J |  | J. |  | Smith |  |  |
Smith, J. |  | J. |  | Smith |  |  |
Smith, JA |  | J. A. |  | Smith |  |  |
Smith, J A |  | J. A. |  | Smith |  |  |
Smith, J.A. |  | J. A. |  | Smith |  |  |
Smith, J. A. |  | J. A. |  | Smith |  |  |
Frenkel ter Hofstede |  | Frenkel |  | ter Hofstede |  |  |
Frenkel Ter Hofstede |  | Frenkel |  | ter Hofstede |  |  |
Frenkel de La Silva |  | Frenkel |  | de la Silva |  |  |
Frenkel la Silva del Ruiz |  | Frenkel |  | la Silva del Ruiz |  |

Examples of names that are __not__ yet correctly parsed:

| input |
|------------------------------------|
| Frog III, MD, Ph.D., Dr. Kermit T. |
| Frog III, Dr. Kermit T., MD, Ph.D. |

Please let me know if you find additional examples of names that are not handled correctly.


Name Compatibility
------------------

NameJuggler also compares names for compatibility, i.e. to determine whether two strings could possibly be alternative representations of the same name.  Of course there is no guarantee that two similar names actually represent the same person; but it is often easy to tell that two names probably do _not_ represent the same person.  Thus, the compatibility tester should be thought of as filter that distinguishes clearly different names, but that makes no claims about the positive cases.  In the simplest case, "A.B." and "AB" are compatible, but many people have those initials.

Some example name pairs that NameJuggler identifies as __incompatible__ include:

|       name A    |   name B
|----------------:|--------------
|      John Smith | John Jones
|      John Smith | Jane Smith
|    John A Smith | John B Smith
|        JA Smith | JB Smith
|Edward O. Wilson | EQ Wilson

Some example name pairs that NameJuggler identifies as __compatible__ include:

|        name A    |   name B
|-----------------:|--------------
|       John Smith | John Smith
|       John Smith | Smith
|    John Q. Smith | John Q. Smith
|      Smith, John | John Smith
|   Smith, John Q. | John Q. Smith
|       John Smith | John Smith MD
|       John Smith | John Smith, MD
|       John Smith | John Smith, MD, PhD
|       John Smith | Smith, John, MD, PhD
|       John Smith | John Smith, MD PhD
|       John Smith | John Smith MD PhD
|       John Smith | J Smith
|      Smith, John | J Smith
|       John Smith | John Archibald Smith
|       John Smith | John A Smith
|       John Smith | J Archibald Smith
|       John Smith | J A Smith
|       John Smith | JA Smith
|          J Smith | JA Smith
|       E O Wilson | Ed Wilson
|       E O Wilson | EO Wilson
|       E O Wilson | ED O Wilson
|       E O Wilson | EDWARD Wilson
| Edward O. Wilson | EO Wilson
|Jacqueline du Pre | Jacqueline Pre
|        Ana Durić | Ana Duric
|Jacqueline du Pré | Jacqueline Pre
|  KERMIT THE FROG | Kermit T. Frog


Quick Start
-----------

To just run the command-line version, please download the [latest build](https://dev-iesl.cs.umass.edu/jenkins/job/namejuggler/lastSuccessfulBuild/artifact/target/namejuggler-assembly-0.1-SNAPSHOT.jar).

To parse names, just pass them to STDIN, one per line; the output is a tab-delimited table like the example above.

    :::sh
    echo "John Q. Public" | java -jar namejuggler-assembly-0.1-SNAPSHOT.jar

To compare names, pass them on a line, delimited by tabs or semicolons, and use the `--compat` flag.  All vs. all comparisons are made among the names on a line, so take care
how many you include.  Multiple lines are processed independently.

    :::sh
    echo "John Q. Public ; J.Q. Public ; J. Quentin Public ; R. Q. Public" | java -jar namejuggler-assembly-0.1-SNAPSHOT.jar --compat

The output groups unambiguous cliques of mutually compatible names on lines starting with CLIQUE.  These cliques have no external links and so are unambiguous.
Lines starting with TRANS show groups of names that are transitively but perhaps not directly compatible.  The
adjacency graph among these ambiguous cases may of course be interesting; one approach might be to find maximal cliques within it.  Please let me know what output format would be helpful if you plan to pursue further analyses of this sort.



Background
----------

A name is not a fixed thing; it is a probabilistic cloud of strings, all denoting the same person.  Here we don't cover the case that a person changes
names completely; in that case there are two disjoint clouds of strings, so that should be modeled by allowing a person to have multiple PersonNames.

Here we try to model different representations of "the same name".  Variations may include: omitting some components; using initials for some components;
reordering; etc.  The most "different" case to model is that of married names vs. maiden names.  Since one or both of these may appear,
but the other name components are not affected, we consider this a case of multiple surnames within one name.


Things that can be done in code
-------------------------------

NameJuggler is mainly meant to be used as a library.  The simplest way to parse a name is:

    :::scala
    val name : PersonNameWithDerivations = PersonNameWithDerivations("John Q. Public").inferFully

Canonical fields can then be reassembled to produce alternate representations of the name (e.g., representing given names only by initials, etc.)

    :::scala
    assert(name.initials === "J. Q. P.")

and so forth.

It is possible to take multiple name variants as input and coordinate them into a single record.  For instance,
if we assert that Amanda Jones and A. Jones-Archer are the same person, then we should later recognize Amanda Archer as a valid variant.  This is accomplshed by parsing the two
input records and merging them; the result is a PersonName that can match various combinations of the fields from both inputs.


Questions and feedback
----------------------

Please contact David Soergel <soergel@cs.umass.edu>.


