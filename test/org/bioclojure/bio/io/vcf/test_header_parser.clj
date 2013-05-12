(ns org.bioclojure.bio.io.vcf.test-header-parser
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [org.bioclojure.bio.io.vcf.test-fixtures :refer :all]
            [org.bioclojure.bio.io.vcf.header-parser :refer :all]))

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
