
;; output collection
[{:test-suite-id "ts-id"
  :test-suite-name "ts-name"
  :test-suite-description "ts-description"
  :test-suite-set [{:test-case-id "tc-id"
                    :test-case-name "tc-name"
                    :test-case-description "tc-description"
                    :test-case-set [{:test-step-id "t-id"
                                     :test-step-text "t-text"
                                     :expect v-expect
                                     :actual v-actual
                                     :result}]}]}]

;; input collection
[{:test-suite-id "ts-id"
  :test-suite-name "ts-name"
  :test-suite-description "ts-description"
  :test-suite-browser driver
  :test-suite-url go-url
  :test-suite-set [{:test-case-id "tc-id"
                    :test-case-name "tc-name"
                    :test-case-description "tc-description"
                    :test-case-browser driver
                    :test-case-url go-url
                    :test-case-set [{:test-step-id "t-id"
                                     :test-step-text "t-text"
                                     :test-browser driver
                                     :test-url go-url
                                     :expect v-expect
                                     :test-function f
                                     :el-query q
                                     :func-opt opt}]}]}]

[{:test-step-id "t-id"
  :test-step-text "t-text"
  :expect v-expect
  :test-function f
  :el-query q
  :func-opt opt}]

{:browser driver
 :url go-url}
