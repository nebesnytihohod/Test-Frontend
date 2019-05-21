(ns test-frontend.reader
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [etaoin.api :refer :all]
            [etaoin.keys :as k]))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)"
  [source]  ; String "/tmp/test-input.edn"
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

;(defn write-edn
;  "write output file in EDN-format"
;  [target]
;  (try
;     (with-open [wrtr (io/writer target)]
;       .writer wrtr)
;
;     (catch Exception e
;       (println "ERROR=> " (.getMessage e)))))

(def *res (atom []))

;(def z (load-edn "/home/maxim/Documents/MyJob/Test-Frontend/src/test_frontend/input_test.edn"))
;(def z (load-edn "C:\\Users\\maksi\\Documents\\MyJob-Sber\\Test-Frontend\\src\\test_frontend\\input_test.edn"))

(defn log!
  [text tc-id tc-n t-s descr v-expect f driver q opt]
  (swap! *res conj {:test-case-id tc-id
                    :test-case-name tc-n
                    :test-step t-s
                    :description descr
                    :text text
                    :expect v-expect
                    :actual (apply f driver q opt)})
  driver)

(defn test-launch
  [test-edn-path]
  (let [test-edn (load-edn test-edn-path)]
    (doseq [i test-edn]
      (def driver (((i :test-suite-browser) {:chrome chrome
                                             :firefox firefox
                                             :safari safari})))
      (if (i :test-suite-url)
        (log! "test preparation"
              (i :test-suite-id)
              (i :test-suite-name)
              "step 0"
              (i :test-suite-description)
              nil
              go
              driver
              (i :test-suite-url)
              nil))
      (doseq [k (i :test-suite-set)]
        (doseq [l (k :test-case-set)]
          (log! (l :test-step-text)
                (k :test-case-id)
                (k :test-case-name)
                (l :test-step-id)
                (k :test-case-description)
                (l :expect)
                (resolve (l :test-function))
                driver
                (l :el-query)
                (l :func-opt))))
      (quit driver))))  ; TODO: add a condition to close the browser window/session -> if (i :close-session == true) then (quit driver)
  ;driver)  ; add return if you need close browser separately in code below

(defn print-res
  []
  (identity @*res))

(comment
  (test-launch "src/test_frontend/input_test.edn")
  (print-res))