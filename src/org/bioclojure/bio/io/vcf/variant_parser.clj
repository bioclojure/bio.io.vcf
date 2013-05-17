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
        parsers (zipmap (keys spec) (map parser-for (vals spec)))]
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

(defn variant-parser
  "Given a parsed VCF header map, return a function that will parse a
  variant row."
  [headers]
  (let [sample-ids    (drop 9 (:columns headers))
        info-parser   (build-parser (get headers "INFO"))
        format-parser (build-parser (get headers "FORMAT"))]
    (fn [s]
      (let [m (zipmap (:columns headers) (str/split s #"\t"))
            f (str/split (m "FORMAT") #":")]
        {:chr    (m "CHROM")
         :pos    (value dec-lit (m "POS"))
         :id     (value (<|> missing (delimited-str \;)) (m "ID"))
         :ref    (m "REF")
         :alt    (str/split (m "ALT") #",")
         :qual   (value (<|> missing dec-lit) (m "QUAL"))
         :filter (value (<|> missing (delimited-str \,)) (m "FILTER"))
         :info   (parse-info info-parser (m "INFO"))
         :gtype  (zipmap sample-ids
                         (map #(parse-format format-parser f (m %)) sample-ids))}))))
