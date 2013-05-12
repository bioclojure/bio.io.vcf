(ns org.bioclojure.bio.io.vcf.parser-util
  (:require [blancas.kern.core :refer [sep-by sym* field* value >> return]]))

(defn delimited-str
  "Returns a parser that will split a string on the given `delim`. "
  [delim]
  (sep-by (sym* delim) (field* (str delim))))

(defn parse-delimited-str
  [delim s]
  (value (delimited-str delim) s))

;; VCF uses '.' to represent a missing value.
(def missing (>> (sym* \.) (return nil)))
