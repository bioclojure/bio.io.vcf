(ns org.bioclojure.bio.io.vcf.variant-parser
  (:require [clojure.string :as str]
            [org.bioclojure.bio.io.vcf.parser-util :refer [delimited-str missing]]
            [blancas.kern.core :refer [value <|> field*]]
            [blancas.kern.lexer.basic :refer [dec-lit float-lit comma-sep]]))

(defn ^:testable ^:private build-parser
  "Construct a map of parsers for INFO or FORMAT fields, according to
  the number and type defined in the headers. Return a function that takes
  a key/value pair and returns a vector of key and (possibly parsed) value."
  [spec]  
  (let [parser-for (fn [{:keys [type number]}]
                     (case number
                       0 (constantly true)
                       1 (case type
                           "Integer" (partial value (<|> missing dec-lit))
                           "Float"   (partial value (<|> missing float-lit))
                           identity)
                       (case type
                         "Integer" (partial value (comma-sep (<|> missing dec-lit)))
                         "Float"   (partial value (comma-sep (<|> missing float-lit)))
                         (partial value (comma-sep (<|> missing (field* ",")))))))
        parsers (if spec (zipmap (keys spec) (map parser-for (vals spec))) {})]
    (fn [[k & [v]]]
      (if-let [p (parsers k)]
        [k (p v)]
        [k v]))))

;; N.B. This doesn't cope with quoted string values in an INFO field.
;; The VCF specification isn't clear on whether or not the value can
;; be quoted.
(defn ^:testable ^:private parse-info
  "Parse a semi-colon-delimited INFO field. Return a map."
  [parser s]
  (when (not= s ".")
    (into {} (map parser (map #(str/split % #"=") (str/split s #";"))))))

;; Again, we make no attempt to handle quoted strings.
(defn ^:testable ^:private parse-format
  "Parse a colon-delimited FORMAT field, return a map."
  [parser fields s]
  (when (not= s ".")
    (into {} (map parser (map vector fields (str/split s #":"))))))

(defmacro parse-if-defined
  [m k f]
  `(when-let [v# (get ~m ~k)]
     (when (not= v# ".") (~f v#))))

(defn- variant-parser*
  [columns info-parser format-parser]
  (let [sample-ids (drop 9 columns)]
    (fn [s]
      (let [m (zipmap columns (str/split s #"\t"))
            f (parse-if-defined m "FORMAT" #(str/split % #":"))]
        {:chr    (m "CHROM")
         :pos    (Long/parseLong (m "POS"))
         :id     (parse-if-defined m "ID" #(str/split % #";"))
         :ref    (m "REF")
         :alt    (parse-if-defined m "ALT" #(str/split % #","))
         :qual   (parse-if-defined m "QUAL" #(Double/parseDouble %))
         :filter (parse-if-defined m "FILTER" #(str/split % #","))
         :info   (parse-if-defined m "INFO" #(parse-info info-parser %))
         :format f
         :gtype  (zipmap sample-ids
                         (map (fn [id]
                                (parse-if-defined m id (partial parse-format format-parser f)))
                              sample-ids))}))))

(defn basic-variant-parser
  "Basic variant parser: faster than the default `variant-parser` as
  it does not parse the INFO and FORMAT field values."
  [headers]
  (let [null-parser (fn [[k & [v]]] [k v])]
    (variant-parser* (:columns headers) null-parser null-parser)))

(defn variant-parser
  [headers]
  (variant-parser* (:columns headers)
                   (build-parser (get headers "INFO"))
                   (build-parser (get headers "FORMAT"))))
