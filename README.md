# UFSAC: Unification of Sense Annotated Corpora and Tools

This repository contains the dataset of the article named "UFSAC: Unification of Sense Annotated Corpora and Tools", written by Loïc Vial, Benjamin Lecouteux and Didier Schwab, for the 11th edition of the Language Resources and Evaluation Conference (LREC) that took place in May 2018 in Miyazaki, Japan.

The full article is available at the following URL: <http://www.lrec-conf.org/proceedings/lrec2018/summaries/250.html>.

## Content of the repository

This repository contains:

* The sense annotated corpora in UFSAC, the format described in the paper, in the folder **`corpus`** (**also available through direct links, see below**). Note that the files have been compressed using the tool `xz` and therefore needs to be decompressed with `unxz` or similar. The last version (2.1) contains the following corpora:
  * SemCor
  * *DSO (code to convert the original data only)*
  * WordNet Gloss Tagged
  * MASC
  * OMSTI
  * *Ontonotes (code to convert the original data only)*
  * Train-O-Matic
  * SensEval 2 WSD task (both from original data and from Raganato et al. (2017) framework are available)
  * SensEval 2 Lexical Sample task
  * SensEval 3 task 1 (both from original data and from Raganato et al. (2017) framework are available)
  * SensEval 3 task 6
  * SemEval 2007 task 7 (both from original data and from Raganato et al. (2017) framework are available)
  * SemEval 2007 task 17 (both from original data and from Raganato et al. (2017) framework are available)
  * SemEval 2013 task 12 (both from original data and from Raganato et al. (2017) framework are available)
  * SemEval 2015 task 13 (both from original data and from Raganato et al. (2017) framework are available)
  
* The source code of the Java API and the scripts described in the paper, in the folder **`java`**.

* Scripts for converting corpora from various formats (Semcor, DSO, OMSTI...) into UFSAC, converting UFSAC corpora into [*Raganato et al.*](http://lcl.uniroma1.it/wsdeval/data/EACL17_WSD_EvaluationFramework.pdf)'s format, computing MFS, etc., in the folder **`scripts`**

## Get Started

If you want to use the Java API or the scripts, the prerequisites are:
- Java 8 or higher - <https://java.com>
- Maven - <https://maven.apache.org>

Once they are installed, you must compile the code:
- Go into the `java` folder
- Run `mvn compile` or `./compile.sh`

And if you want to use the library as a dependency in another Maven projects:
- Go into the `java` folder
- Run `mvn install` or `./install.sh`

## Version history

### Version 2.1 (October 2018)

Direct link to the data: <https://drive.google.com/file/d/1Oigo3kzRosz2VjyA44vpJZ58tDFyLRMO>

- Small fix in Semeval2007Task7, Semeval2015Task13 and Raganato et al. corpora where words in a multi-word expression were collapsed. They are now separated by an underscore symbol.
- Version number is shorter: `<major version>.<minor version>`

### Version 2.0.0 (July 2018)

Direct link to the data: <https://drive.google.com/file/d/1FOpl6AUHVRLbVp5zPQQmjc-HVsssT1up>

- Add new corpora:
  - *Raganato et al.*'s versions of SensEval/SemEval corpora (6 separate corpora, original data: <http://lcl.uniroma1.it/wsdeval/data/WSD_Unified_Evaluation_Datasets.zip>) 
  - Training and testing data of SensEval 2 lexical sample task (2 separate corpora, original data: <http://www.hipposmond.com/senseval2/Results/senseval2-corpora.tgz>)
  - Training and testing data of SensEval 3 Task 6 (lexical sample) (2 separate corpora, original data: <http://web.eecs.umich.edu/~mihalcea/senseval/senseval3/data/EnglishLS/EnglishLS.train.tar.gz> and <http://web.eecs.umich.edu/~mihalcea/senseval/senseval3/data/EnglishLS/EnglishLS.test.tar.gz>)
  - Train-O-Matic (original data: <http://trainomatic.org/data/train-o-matic-data.zip>)
- Add a "normalizing punctuation" step after every corpus conversion
- Small fix in OMSTI

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

