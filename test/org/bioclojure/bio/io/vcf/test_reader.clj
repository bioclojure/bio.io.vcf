(ns org.bioclojure.bio.io.vcf.test-reader
  (require [midje.sweet :refer :all]
           [org.bioclojure.bio.io.vcf.test-fixtures :refer :all]
           [org.bioclojure.bio.io.vcf.reader :as vcf]))

(with-open [v (vcf/vcf-reader vcf)]

  (fact "vcf/header returns the expected data for entire header"
        (vcf/header v)
        => parsed-headers)

  (fact "vcf/header returns expected data for FORMAT header"
        (vcf/header v "FORMAT")
        => (parsed-headers "FORMAT"))

  (fact "vcf/header returns expected data for INFO/H2 header"
        (vcf/header v "INFO" "H2")
        => (get-in parsed-headers ["INFO" "H2"]))

  (fact "vcf/sample-ids returns the expected sample ids"
        (vcf/sample-ids v)
        => expected-sample-ids)

  (let [variants (vcf/variant-seq v)]

    (fact "vcf/variant-seq returns the expected number of variants"
          (count variants) => 5)

    (fact "The first variant returned by vcf/variant-seq is as expected"
          (first variants) => parsed-variant)))

(fact "We can read a compressed VCF"
      (with-open [v (vcf/vcf-reader compressed-vcf)]
        (count (vcf/variant-seq v)) => 5))