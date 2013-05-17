(ns org.bioclojure.bio.io.vcf.test-fixtures
  (:require [clojure.java.io :as io]))

(def vcf (io/resource "example.vcf"))

(def compressed-vcf (io/resource "example.vcf.gz"))

(def vcf-data (with-open [rdr (io/reader vcf)]
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
(def variant "20\t14370\trs6054257\tG\tA\t29\tPASS\tNS=3;DP=14;AF=0.5;DB;H2\tGT:GQ:DP:HQ\t0|0:48:1:51,51\t1|0:48:8:51,51\t1/1:43:5:.,.")

(def expected-columns ["CHROM" "POS" "ID" "REF" "ALT" "QUAL" "FILTER" "INFO" "FORMAT" "NA00001" "NA00002" "NA00003"])

(def parsed-headers (assoc expected-metadata :columns expected-columns))

(def parsed-variant
  {
   :chr "20"
   :pos 14370
   :id ["rs6054257"]
   :ref "G"
   :alt ["A"]
   :qual 29.0
   :filter ["PASS"]
   :info {"NS" 3 "DP" 14 "AF" [0.5] "DB" true "H2" true}
   :format ["GT" "GQ" "DP" "HQ"]
   :gtype {"NA00001" {"GT" "0|0" "GQ" 48 "DP" 1 "HQ" [51 51]}
           "NA00002" {"GT" "1|0" "GQ" 48 "DP" 8 "HQ" [51 51]}
           "NA00003" {"GT" "1/1" "GQ" 43 "DP" 5 "HQ" [nil nil]}}})

(def expected-sample-ids ["NA00001" "NA00002" "NA00003"])