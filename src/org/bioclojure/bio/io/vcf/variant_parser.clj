(ns org.bioclojure.bio.io.vcf.variant-parser
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.lexer.basic :refer :all]))

(def ^:private tab-sep-str (sep-by tab (field* "\t")))

(def ^:private semi-sep-str (sep-by (sym* \;)  (field* ";")))

(def ^:private colon-sep-str (sep-by (sym* \:) (field* ":")))

(def ^:private comma-sep-str (sep-by (sym* \,) (field* ",")))

(def ^:private missing (>> (sym* \.) (return nil)))

(defn- get-defined
  [m k]
  (when-let [v (get m k)]
    (when (not= v ".") v)))

(defn- parse-int
  [m k]
  (when-let [v (get-defined m k)]
    (value dec-lit v)))

(defn- parse-csv
  [m k]
  (if-let [v (get-defined m k)]
    (value comma-sep-str v)
    []))

(defn- build-parsers
  "Construct a map of parsers for INFO or FORMAT fields, according to
  the number and type defined in the headers."
  [spec]
  (letfn [(parser-for [{:keys [type number]}]
            (case number
              0 (constantly true)
              1 (case type
                  "Integer" (partial value (<|> missing dec-lit))
                  "Float"   (partial value (<|> missing float-lit))
                  identity)
              (case type
                "Integer" (partial value (comma-sep (<|> missing dec-lit)))
                "Float"   (partial value (comma-sep (<|> missing float-lit)))
                identity)))]  
    (zipmap (keys spec) (map parser-for (vals spec)))))

(defn- parse-info
  [m k parser-for]
  (if-let [v (get-defined m k)]
    (into {} (for [[k v] (map #(value (sep-by (sym* \=) (field* "=")) %) (value semi-sep-str v))
                   :let [parse-value (or (parser-for k) identity)]]
               [k (parse-value v)]))))

;; XXX TODO: parse genotypes and add to the data structure below
(defn variant-parser
  [headers]
  (let [sample-ids       (drop 9 (:columns headers))
        info-parsers     (build-parsers (get headers "INFO"))
        genotype-parsers (build-parsers (get headers "FORMAT"))]
    (fn [s]
      (let [raw (zipmap (:columns headers) (value tab-sep-str s))]
        {:chr    (get raw "CHROM")
         :pos    (parse-int raw "POS")
         :id     (parse-csv raw "ID")
         :ref    (get raw "REF")
         :alt    (parse-csv raw "ALT")
         :qual   (parse-int raw "QUAL")
         :filter (parse-csv raw "FILTER")
         :info   (parse-info raw "INFO" info-parsers)}))))
