# bio.io.vcf

A Clojure library designed for reading VCF (Variant Call Format) data,
as defined by the 1000 Genomes Project:

http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41

## Usage

Load the reader:

    (require '[org.bioclojure.bio.io.vcf.reader :as vcf])

### vcf-reader

The main entry point is the `vcf-reader` function. This takes a VCF as
its only argument, which can be anything understood by
`clojure.java.io/input-stream` (string, File, URI, ...). A
Gzip-compressed input stream will be detected and automatically
decompressed.

`vcf-reader` should be called inside of `with-open` to ensure the
underlying input stream is closed.

    (with-open [v (vcf/vcf-reader (io/resource "sample.vcf"))]
       (doall (vcf/variant-seq v)))

### header

Returns the requested header from a vcf:

    (vcf/header v "INFO" "H2")
    ;; => {:id "H2", :number 0, :type "Flag", :description "HapMap2 membership"}
    
### sample-ids  

Returns the list of sample ids from the VCF.

    (vcf/sample-ids v)
    ;; => ("NA00001" "NA00002" "NA00003")

### variant-seq

Returns a lazy sequence of variants from the VCF.

    (first (vcf/variant-seq v))
    ;; => {:chr "20",
    ;;     :info {"NS" 3, "DP" 14, "AF" [0.5], "DB" true, "H2" true},
    ;;     :alt ["A"],
    ;;     :ref "G",
    ;;     :filter ["PASS"],
    ;;     :pos 14370,
    ;;     :gtype
    ;;       {"NA00003" {"GT" "1/1", "GQ" 43, "DP" 5, "HQ" [nil nil]},
    ;;        "NA00002" {"GT" "1|0", "GQ" 48, "DP" 8, "HQ" [51 51]},
    ;;        "NA00001" {"GT" "0|0", "GQ" 48, "DP" 1, "HQ" [51 51]}},
    ;;     :qual 29,
    ;;     :id ["rs6054257"]}

### pass? 

Utility function for filtering PASSed variants in the VCF.

    (with-open [v (vcf/vcf-reader (io/resource "example.vcf"))]
        (doall (filter vcf/pass? (vcf/variant-seq v))))

## License

Copyright © 2013 Ray Miller <ray@1729.org.uk>

Distributed under the Eclipse Public License, the same as Clojure.