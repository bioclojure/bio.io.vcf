(ns org.bioclojure.bio.io.vcf.test-variant-parser
  (require [midje.sweet :refer :all]
           [org.bioclojure.bio.io.vcf.test-fixtures :refer :all]
           [org.bioclojure.bio.io.vcf.variant-parser :refer :all]))

(fact "Parsing variant returns the expected map"
      ((variant-parser parsed-headers) variant)
      => parsed-variant)