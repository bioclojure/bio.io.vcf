(ns org.bioclojure.bio.io.vcf.reader
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [org.bioclojure.bio.io.vcf.header-parser :as hp]
            [org.bioclojure.bio.io.vcf.variant-parser :refer [variant-parser]])
  (:import [java.util.zip GZIPInputStream]
           [java.io IOException]))

(def ^:private header? (partial re-find #"^#"))

(defn- open-input-stream
  "Attempt to open the stream `s` as a GZIP input stream. If it is not
  in GZIP format, reset and return the raw stream."
  [s]
  (let [raw-stream (io/input-stream s)]
    (try
      (.mark raw-stream 1024)
      (GZIPInputStream. raw-stream)
      (catch IOException e
        (if (= (.getMessage e) "Not in GZIP format")
          (doto raw-stream (.reset))
          (throw e))))))

;; The VcfReader record stores a java.io.BufferedReader and the parsed
;; VCF headers. We implement the java.io.Closeable interface so that a
;; VcfReader object can be usen in with-open.
(defrecord VcfReader [reader headers]
  java.io.Closeable
  (close [_]
    (.close reader)))

(defn vcf-reader
  [vcf]
  (let [reader (io/reader (open-input-stream vcf))
        headers (hp/parse-headers (take-while header? (line-seq reader)))]
    (VcfReader. reader headers)))

(defn sample-ids
  [vcf]
  (drop 9 (get-in vcf [:headers :columns])))

(defn header
  [vcf & ks]
  (get-in vcf (into [:headers] ks)))

(defn variant-seq
  [vcf]
  (map (variant-parser (:headers vcf)) (line-seq (:reader vcf))))
