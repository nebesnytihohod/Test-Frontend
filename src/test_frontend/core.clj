(ns test-frontend.core)

(require 'test-frontend.verita-test-ui)

(defn -main
  "myfirst app with etaoin framework"
  [& args]
  (test-frontend.verita-test-ui/test-ui))
