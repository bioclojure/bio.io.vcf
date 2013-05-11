(ns org.bioclojure.bio.io.vcf.test-header-parser
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [org.bioclojure.bio.io.vcf.header-parser :refer :all]))

(def vcf-data (with-open [rdr (io/reader (io/resource "example.vcf"))]
                (doall (line-seq rdr))))

(def vcf-headers
  (take-while (partial re-find #"^#") vcf-data))

(def vcf-metadata-headers
  (take-while (partial re-find #"^##") vcf-headers))

(def expected-metadata {"FORMAT" {"HQ"
                                  {:id "HQ",
                                   :number 2,
                                   :type "Integer",
                                   :description "Haplotype Quality"},
                                  "DP"
                                  {:id "DP", :number 1, :type "Integer", :description "Read Depth"},
                                  "GQ"
                                  {:id "GQ",
                                   :number 1,
                                   :type "Integer",
                                   :description "Genotype Quality"},
                                  "GT" {:id "GT", :number 1, :type "String", :description "Genotype"}},
                        "FILTER" {"s50" {:id "s50", :description "Less than 50% of samples have data"},
                                  "q10" {:id "q10", :description "Quality below 10"}},
                        "INFO" {"H2"
                                {:id "H2",
                                 :number 0,
                                 :type "Flag",
                                 :description "HapMap2 membership"},
                                "DB"
                                {:id "DB",
                                 :number 0,
                                 :type "Flag",
                                 :description "dbSNP membership, build 129"},
                                "AA"
                                {:id "AA",
                                 :number 1,
                                 :type "String",
                                 :description "Ancestral Allele"},
                                "AF"
                                {:id "AF",
                                 :number \.,
                                 :type "Float",
                                 :description "Allele Frequency"},
                                "DP"
                                {:id "DP", :number 1, :type "Integer", :description "Total Depth"},
                                "NS"
                                {:id "NS",
                                 :number 1,
                                 :type "Integer",
                                 :description "Number of Samples With Data"}},
                        "phasing" "partial",
                        "reference" "1000GenomesPilot-NCBI36",
                        "source" "myImputationProgramV3.1",
                        "fileDate" "20090805",
                        "fileformat" "VCFv4.0"})

(def file-header   "##fileformat=VCFv4.0")
(def info-header   "##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">")
(def filter-header "##FILTER=<ID=q10,Description=\"Quality below 10\">")
(def format-header "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">")
(def column-header "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tNA00001\tNA00002\tNA00003")

(def expected-columns ["CHROM" "POS" "ID" "REF" "ALT" "QUAL" "FILTER" "INFO" "FORMAT" "NA00001" "NA00002" "NA00003"])

(fact "We can parse a simple key/value header line"
      (parse-header-line file-header)
      => ["fileformat" "VCFv4.0"])

(fact "We can parse an INFO header line"
      (parse-header-line info-header)
      => ["INFO" {:id "NS" :number 1 :type "Integer" :description "Number of Samples With Data"}])

(fact "We can parse a FILTER header line"
      (parse-header-line filter-header)
      => ["FILTER" {:id "q10" :description "Quality below 10"}])

(fact "We can parse a FORMAT header line"
      (parse-header-line format-header)
      => ["FORMAT" {:id "GT" :number 1 :type "String" :description "Genotype"}])

(fact "We can parse the column header"
      (parse-column-header column-header)
      => expected-columns)

(fact "Parsing the metadata headers returns the expected map"
      (parse-metadata-headers vcf-metadata-headers)
      => expected-metadata)

(fact "Parsing all headers returns a map with metadata and column names"
      (parse-headers vcf-headers)
      => (assoc expected-metadata :columns expected-columns))
