(ns org.bioclojure.bio.io.vcf.header-parser
  (:require [clojure.java.io :as io]
            [blancas.kern.core :refer [token* field* sym* bind many any-char return sep-by value >> <|>]]
            [blancas.kern.lexer.basic :refer [string-lit dec-lit angles comma-sep token one-of]]))

(def ^:private header-prefix
  (token* "##"))

(def ^:private header-key
  (field* "="))

(def ^:private simple-val
  (bind [v (many any-char)]
        (return (apply str v))))

(def ^:private id-field
  (bind [id (>> (token* "ID=") (field* ",>"))]
        (return [:id id])))

(def ^:private type-field
  (bind [type (>> (token* "Type=") (token "Integer" "Float" "Flag" "Character" "String"))]
        (return [:type type])))

(def ^:private number-field
  (bind [num (>> (token* "Number=") (<|> dec-lit (one-of ".AG")))]
        (return [:number num])))

(def ^:private description-field
  (bind [descr (>> (token* "Description=") string-lit)]
        (return [:description descr])))

(def ^:private metadata-field
  (<|> id-field type-field number-field description-field))

(def ^:private key-val-list
  (bind [kvs (angles (comma-sep metadata-field))]
        (return (into {} kvs))))

(def ^:private header-val
  (<|> key-val-list string-lit simple-val))

(def ^:private header
  (bind [_ header-prefix
         k header-key
         _ (sym* \=)
         v header-val]
        (return [k v])))

(def ^:private column-header
  (>> (sym* \#) (sep-by (sym* \tab) (field* "\t"))))

(defn parse-header-line
  "Parse a single VCF metadata header line. Return a vector of
   [key,value] where the value may be a map or a simple string."
  [s]
  (or (value header s)
      (throw (Exception. (str "Failed to parse VCF header line: '" s "'")))))

(defn parse-column-header
  "Parse the tab-delimeted VCF column header. Return a vector of
  column names."
  [s]
  (or (value column-header s)
      (throw (Exception. (str "Failed to parse VCF column header: '" s "'")))))

(defn parse-metadata-headers
  "Parse all the VCF metadata headers. Return a nested map."
  [xs]
  (reduce (fn [accum [k v]]            
            (if
             (and (map? v) (:id v)) (assoc-in accum [k (:id v)] v)
             (assoc accum k v)))
          {}
          (map parse-header-line xs)))

(defn parse-headers
  "Parse the VCF metadata and column headers. Return a nested map with
   the column names under the :columns key."
  [xs]
  (let [[metadata-headers [column-header]] (split-with (partial re-find #"^##") xs)
        metadata (parse-metadata-headers metadata-headers)
        columns  (parse-column-header column-header)]
    (assoc metadata :columns columns)))
