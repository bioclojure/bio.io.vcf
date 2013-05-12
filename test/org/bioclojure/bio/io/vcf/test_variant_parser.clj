(ns org.bioclojure.bio.io.vcf.test-variant-parser
  (require [midje.sweet :refer :all]
           [midje.util :refer [expose-testables]]
           [org.bioclojure.bio.io.vcf.test-fixtures :refer :all]
           [org.bioclojure.bio.io.vcf.variant-parser :refer :all]
           ))

(expose-testables org.bioclojure.bio.io.vcf.variant-parser)

(let [p (build-parser {"TEST" {:id "TEST" :type "Integer" :number 3 :description "Just testing"}})]

  (fact "We can parse a list of 3 integers from an info field"
        (parse-info p "TEST=1,23,4") => {"TEST" [1 23 4]})

  (fact "We can parse a list of 3 integers including a nil value at the start"
        (parse-info p "TEST=.,23,4") => {"TEST" [nil 23 4]})

  (fact "We can parse a list of 3 integers including a nil value in the middle"
        (parse-info p "TEST=1,.,4") => {"TEST" [1 nil 4]})

  (fact "We can parse a list of 3 integers including a nil value at the end"
        (parse-info p "TEST=1,23,.") => {"TEST" [1 23 nil]}))

(let [p (build-parser {"TEST" {:id "TEST" :type "String" :number 3 :description "Just testing"}})]

  (fact "We can parse a list of 3 strings from an info field"
        (parse-info p "TEST=a,bc,d") => {"TEST" ["a" "bc" "d"]})

  (fact "We can parse a list of 3 strings with a nil value at the start"
        (parse-info p "TEST=.,bc,d") => {"TEST" [nil "bc" "d"]})

  (fact "We can parse a list of 3 strings with a nil value in the middle"
        (parse-info p "TEST=a,.,d")  => {"TEST" ["a" nil "d"]})

  (fact "We can parse a list of 3 strings with a nil value at the end"
        (parse-info p "TEST=a,bc,.") => {"TEST" ["a" "bc" nil]}))

(let [p (build-parser {"S1" {:id "S1" :type "String" :number 1}
                       "S2" {:id "S." :type "String" :number \.}
                       "F1" {:id "F1" :type "Float" :number 1}
                       "F2" {:id "F2" :type "Float" :number \.}})]

  (fact "We can parse a format string with a single string field"
        (parse-format p ["S1"] "something") => {"S1" "something"})

  (fact "We can parse a format string with multiple fields"
        (parse-format p ["S1" "F2" "S2" "F1"] "foo:1.23,4.56:bar,baz:7.89")
        => {"S1" "foo" "F2" [1.23 4.56] "S2" ["bar" "baz"] "F1" 7.89})

  ;; Note that, because S2 was defined as a list, we get a vector
  ;; containing nil for the '.' value.
  (fact "We can parse a format string with a null list value"
        (parse-format p ["S1" "S2" "F1"] "foo:.:1.0")
        => {"S1" "foo" "S2" [nil] "F1" 1.0})

  (fact "We can parse a format string with a null value in a list"
        (parse-format p ["S2" "S1"] "a,.:foo") {"S1" "foo" "S2" ["a" nil]})
  
  (fact "We can parse a format with a null value in a string"
        (parse-format p ["S1" "S2"] ".:foo,bar") {"S1" nil "S2" ["foo","bar"]}))

(fact "Parsing variant returns the expected map"
      ((variant-parser parsed-headers) variant)
      => parsed-variant)