(defproject org.bioclojure/bio.io.vcf "0.1.0"
  :description "Library for working with VCF (Variant Call Format) files"
  :url "https://github.com/bioclojure/bio.io.vcf"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.blancas/kern "0.7.0"]
                 [org.clojars.chapmanb/sam "1.73"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})

