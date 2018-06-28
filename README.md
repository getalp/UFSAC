# UFSAC: Unification of Sense Annotated Corpora and Tools

This repository contains the dataset of the article named "UFSAC: Unification of Sense Annotated Corpora and Tools", written by Loïc Vial, Benjamin Lecouteux and Didier Schwab, for the 11th edition of the Language Resources and Evaluation Conference (LREC) that took place in May 2018 in Miyazaki, Japan.

The full article is available at the following URL: <https://hal.archives-ouvertes.fr/hal-01718237>.

## Content of the repository

This repository contains:

* The sense annotated corpora in UFSAC, the format described in the paper, in the folder **`corpus`** (**also available through direct links, see below**). Note that the files have been compressed using the tool `xz` and therefore needs to be decompressed with `unxz` or similar.

* The source code of the Java API and the scripts described in the paper, in the folder **`java`**.

## Version history

### Version 1.1.0 (June 2018)

Direct link to the data: <https://drive.google.com/file/d/12tIfu85GrbeQcMs3H5VQOvzSqNWvYuol>

- Fix a problem where some POS tags did not follow the PTB convention
- Merge the "omsti_part{0,1,2,3,4}.xml" files in one single "omsti.xml" file

### Version 1.0.0 (May 2018)

Direct link to the data: <https://drive.google.com/file/d/1kF8WKpMlWtBB13y6O20pj5AcWUmnXz-b>

Original version which contains the following corpora:
- Semcor (original data: <http://web.eecs.umich.edu/~mihalcea/downloads/semcor/semcor1.6.tar.gz>)
- WordNet Gloss Tagged (original data: <http://wordnetcode.princeton.edu/glosstag-files/WordNet-3.0-glosstag.tar.bz2>)
- MASC (original data: <https://github.com/google-research-datasets/word_sense_disambigation_corpora>)
- OMSTI (original data: <http://www.comp.nus.edu.sg/~nlp/sw/one-million-sense-tagged-instances-wn30.tar.gz>)
- SensEval 2 (original data: <http://www.hipposmond.com/senseval2/Results/senseval2-corpora.tgz>)
- SensEval 3 (original data: <http://web.eecs.umich.edu/~mihalcea/senseval/senseval3/data/EnglishAW/EnglishAW.test.tar.gz>)
- SemEval 2007 task 07 (original data: <http://nlp.cs.swarthmore.edu/semeval/tasks/task07/data.shtml>)
- SemEval 2007 task 17 (original data: <http://nlp.cs.swarthmore.edu/semeval/tasks/task17/data.shtml>)
- SemEval 2013 task 12 (original data: <https://www.cs.york.ac.uk/semeval-2013/task12/index.php%3Fid=data.html>)
- SemEval 2015 task 13 (original data: <http://alt.qcri.org/semeval2015/task13/index.php?id=data-and-tools>)

Plus the code to produce the UFSAC version from the original version of the following corpora:
- DSO (purchase here: <https://catalog.ldc.upenn.edu/LDC97T12>)
- Ontonotes (freely available here: <https://catalog.ldc.upenn.edu/LDC2013T19>)

