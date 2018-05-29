This repertory contains translation in arabic language of the UFASAC corpus.

For Omsti, you need to concatenate the files for each part. For instance,

with omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz-a and omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz-b you will get omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz

On linux or MacOs: cat omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz-a omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz-b > omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz-a and omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml.xz

Note that every corpus has been compressed using the tool xz and therefore needs to be decompressed with unxz or similar.
