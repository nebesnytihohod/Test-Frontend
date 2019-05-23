(ns test-frontend.reader
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [etaoin.api :refer :all]
            [etaoin.keys :as k]))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)"
  [source]                                                  ; String "/tmp/test-input.edn"
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

;; vector of maps containing result of testing
(def ^:dynamic *res* (atom []))

;; mutable variable for storage number of 'false' test results (i.e. test fail)
(def summary (ref 0))

;; deprecated function
(defn log!
  "Function for execute and simple logging of result into atom structure"
  [text tc-id tc-n t-s descr v-expect f driver q opt]
  (swap! *res* conj {:test-case-id  tc-id
                     :test-case-name tc-n
                     :test-step      t-s
                     :description    descr
                     :text           text
                     :expect         v-expect
                     :actual         (f driver q opt)})
  driver)

(def pm-dir
  (or (System/getenv "DEVOPS_ARTIFACTS")                    ; you are on CI environment
      (str (System/getProperty "user.dir") "/ui_tests/pm_artifacts"))) ; for local machine projects standard path

(def pm-file
  (str pm-dir "/pm-stacktrace.txt"))

(def pm-opt
  {:dir pm-dir})                                            ; TODO: add capability for set target directory for different postmortem artifacts

(def result-dir
  (or (System/getenv "TESTS_ARTIFACTS")                     ; you are on CI environment
      (str (System/getProperty "user.dir") "/ui_tests/test_artifacts"))) ; for local machine projects standard path

(def result-file
  (str result-dir "/output-test.log"))

(defn log-with-pm!
  "Function for execute and logging of result with Postmortem macro"
  ([text tc-id tc-n t-s descr v-expect f driver q]
   (log-with-pm! text tc-id tc-n t-s descr v-expect f driver q nil))
  ([text tc-id tc-n t-s descr v-expect f driver q opt]
   (let [actual (with-postmortem driver pm-opt (if opt
                                                 (f driver q opt)
                                                 (f driver q)))]
     (swap! *res* conj {:test-case-id  tc-id
                        :test-case-name tc-n
                        :test-step      t-s
                        :description    descr
                        :text           text
                        :expect         v-expect
                        :actual         actual
                        :conclusion     (identical? v-expect (if (map? actual) (:value actual) actual))})
     (if (false? (:conclusion (last @*res*))) (dosync (ref-set summary (inc @summary)))))
   driver))

(defn test-launch
  "Function for:
    - call reading test data from file in EDN format;
    - launching tests using Etaoin library.


  Returns:


  **`atom`** - vector of maps containing result of testing. This atom is written to a file.


  **`refs'** - mutable variable for storage number of 'false' test results (i.e. test fail). This var returning back into
  `clojure.test` test function.


  Arguments:


  **`test-edn-path`** - _String_. In the filesystem path to the file that contains test data in EDN format."
  [test-edn-path]
  (try
    (let [test-edn (load-edn test-edn-path)]
      (doseq [ts-level test-edn]
        (let [driver ((resolve (:test-suite-browser ts-level)))]
          (when (:test-suite-url ts-level)
            (log-with-pm! "test preparation"
                          (:test-suite-id ts-level)
                          (:test-suite-name ts-level)
                          "step 0"
                          (:test-suite-description ts-level)
                          nil
                          go
                          driver
                          (:test-suite-url ts-level)))
          (doseq [tc-level (:test-suite-set ts-level)
                  test-level (:test-case-set tc-level)]
            (log-with-pm! (:test-step-text test-level)
                          (:test-case-id tc-level)
                          (:test-case-name tc-level)
                          (:test-step-id test-level)
                          (:test-case-description tc-level)
                          (:expect test-level)
                          (resolve (:test-function test-level))
                          driver
                          (:el-query test-level)
                          (:func-opt test-level)))
          (quit driver)))                                  ; TODO: add a condition to close the browser window/session -> if (i :close-session == true) then (quit driver)
      @summary)
    (catch Exception e
      (spit pm-file (.getMessage e))
      (throw e))
    (finally
      (spit result-file (identity @*res*)))))

(comment
  (test-launch "src/test_frontend/input_test.edn"))
