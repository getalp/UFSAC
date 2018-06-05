# UFSAC: Unification of Sense Annotated Corpora and Tools

This repository contains the dataset of the article named "UFSAC: Unification of Sense Annotated Corpora and Tools", written by Loïc Vial, Benjamin Lecouteux and Didier Schwab, for the 11th edition of the Language Resources and Evaluation Conference (LREC) that took place in May 2018 in Miyazaki, Japan.

The full article is available at the following URL: <https://hal.archives-ouvertes.fr/hal-01718237>.

## Content of the repository

This repository contains:

* The sense annotated corpora in UFSAC, the format described in the paper, in the folder **`corpus`**. Note that every corpus has been compressed using the tool `xz` and therefore needs to be decompressed with `unxz` or similar.

* The source code of the Java API and the scripts described in the paper, in the folder **`java`**.

## Version history

### Version 1.0.0 (May 2018)

Original version which contains the following corpora:
- Semcor (original corpus: <http://web.eecs.umich.edu/~mihalcea/downloads/semcor/semcor1.6.tar.gz>)
- WordNet Gloss Tagged (original corpus: <http://wordnetcode.princeton.edu/glosstag-files/WordNet-3.0-glosstag.tar.bz2>)
- MASC
- OMSTI
- SensEval 2
- SensEval 3
- SemEval 2007 task 07
- SemEval 2007 task 17
- SemEval 2013 task 12
- SemEval 2015 task 13

Plus the code to produce the UFSAC version from the original version of the following corpora:
- DSO
- Ontonotes

