(ns org.bioclojure.bio.io.vcf.test-parser-util
  (:require [midje.sweet :refer :all]
            [blancas.kern.core :as kern]
            [org.bioclojure.bio.io.vcf.parser-util :as u]))

(fact "We can parse a tab-separated string"
      (u/parse-delimited-str \tab "a\tbc\td e f\tg")
      => ["a" "bc" "d e f" "g"])

(fact "Parsing a tab-delimited string with one column returns a one-element vector"
      (u/parse-delimited-str \tab "a")
      => ["a"])

(fact "We can parse a colon-delimited string"
      (u/parse-delimited-str \: "a:b:cde:f")
      => ["a" "b" "cde" "f"])

(fact "A missing value parses to nil"
      (kern/value u/missing ".")
      => nil)
