(ns test-frontend.verita-test-ui
  (:require [etaoin.api :refer :all]
            [etaoin.keys :as k]))

(def test-log [])
(def *res (atom []))

(defn print-res
  []
  (identity @*res))

(defn log
  ;[driver text f & args]
  [text f driver & args]
  (println text (apply f driver args)))
  ;driver)

(defn add-test-log
  [tl-data tc-id tc-n t-s descr v-expect v-actual]
  (conj tl-data {:test-case-id tc-id
                 :test-case-name tc-n
                 :test-step t-s
                 :description descr
                 :expect v-expect
                 :actual v-actual}))

(def pm-dir
  (or (System/getenv "DEVOPS_ARTIFACTS")  ; you are on CI environment
      (str (System/getProperty "user.dir") "/ui_tests/pm_artifacts")))          ; for local machine projects standard path

(def pm-file
  (str pm-dir "/pm-stacktrace.txt"))

(def pm-opt
  {:dir pm-dir})  ; TODO: add capability for set target directory for different postmortem artifacts

(defn log!
  [text tc-id tc-n t-s descr v-expect f driver & args]
  (swap! *res conj {:test-case-id tc-id
                    :test-case-name tc-n
                    :test-step t-s
                    :description descr
                    :text text
                    :expect v-expect
                    :actual (with-postmortem driver pm-opt (apply f driver args))})
  driver)

(defn log2
  "Internal/local logger"
  [text tl-data tc-id tc-n t-s descr v-expect v-actual f driver & args]
  ;[driver text tl-data tc-id tc-n t-s descr v-expect v-actual f & args]
  ;[driver text tl-data tc-id tc-n t-s descr v-expect v-actual]

  ;(add-test-log tl-data tc-id tc-n t-s descr v-expect (apply f driver args))
  ;(log driver text f args)
  ;(log text f driver args)
  (println text)
  (conj *res (add-test-log tl-data tc-id tc-n t-s descr v-expect v-actual)))

(defn test-ui
  "UI automatically testing
  used Etaoin library"
  [driver-name]
  (try
    (let [driver (driver-name)]
           ;; ---===test suite for anonymous user===---
      (log! "1. go to"
            "tc-1"
            "test case 1"
            1
            "test case for connecting to the host"
            "nil"
            go driver "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/")
      (log! "2. wait..."
            "tc-1"
            "test case 1"
            2
            "test case for connecting to the host"
            "nil"
            wait-visible driver [{:tag :div :class "menu"} {:tag :li :class "menu__nav__nav-item"}])

      (log! "3. False? "
            "tc-2"
            "test case 2"
            3
            "test case for create new NS by anonymous user"
            "false"
            has-text? driver "Для создания нового пространства имен выполните вход в систему")  ; expect = false
      (log! "4. click"
            "tc-2"
            "test case 2"
            4
            "test case for create new NS by anonymous user"
            "nil"
            click driver [{:tag :li :id "create-ns"}])
      (log! "5. True? "
            "tc-2"
            "test case 2"
            5
            "test case for create new NS by anonymous user"
            "true"
            has-text? driver "Для создания нового пространства имен выполните вход в систему")  ; expect = true

      (log! "6. False? "
            "tc-id"
            "tc-name"
            "t-s"
            "description"
            "v-expect"
            visible? driver [{:tag :aside :id "main-sider"}])  ; expect = false
      (log! "7. click"
            "tc-id"
            "tc-name"
            "t-s"
            "description"
            "v-expect"
            click driver [{:tag :li :id "workspace"} {:tag :a}])
      (log! "8. wait..."
            "tc-id"
            "tc-name"
            "t-s"
            "description"
            "v-expect"
            wait-visible driver {:tag :div :id "main-sider__not-collapsed-tree"})
      (log! "9. True? "
            "tc-id"
            "tc-name"
            "t-s"
            "description"
            "v-expect"
            visible? driver [{:tag :aside :id "main-sider"}])  ; expect = true

      ;; test case for close/open side panel with namespace
      (log! "10. False? "
            "tc-id"
            "tc-name"
            "t-s"
            "test case for close/open side panel with namespace"
            "false"
            has-class? driver [{:tag :aside :id "main-sider"}] "main-sider ant-layout-sider ant-layout-sider-light ant-layout-sider-collapsed ant-layout-sider-has-trigger")  ; expect = false
      (log! "11. click"
            "tc-id"
            "tc-name"
            "t-s"
            "test case for close/open side panel with namespace"
            "v-expect"
            click driver [{:tag :div :class "ant-layout-sider-trigger"}])
      (wait 3)
      (log! "12. True? "
            "tc-id"
            "tc-name"
            "t-s"
            "test case for close/open side panel with namespace"
            "true"
            has-class? driver [{:tag :aside :id "main-sider"}] "main-sider ant-layout-sider ant-layout-sider-light ant-layout-sider-collapsed ant-layout-sider-has-trigger")  ; expect = true
      (log! "13. click"
            "tc-id"
            "tc-name"
            "t-s"
            "test case for close/open side panel with namespace"
            "v-expect"
            click driver [{:tag :div :class "ant-layout-sider-trigger"}])
      (wait 3)
      (log! "14. False? "
            "tc-id"
            "tc-name"
            "t-s"
            "test case for close/open side panel with namespace"
            "false"
            has-class? driver [{:tag :aside :id "main-sider"}] "main-sider ant-layout-sider ant-layout-sider-light ant-layout-sider-collapsed ant-layout-sider-has-trigger")  ; expect = false

      ;; test case for view tree namespaces
      ;(click driver {:tag :i :class "anticon anticon-plus-square ant-tree-switcher-line-icon"})  ; циклически только открывает любые ветви дерева, но не закрывает
      (log! "15. click"
            "tc-id"
            "tc-name"
            "t-s"
            "test case for view tree namespaces"
            "v-expect"
            click driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/span[11]"}])  ; click driver on 1st level folding icon for expand or collapse
      (log! "16. wait..."
            "tc-id"
            "tc-name"
            "t-s"
            "test case for view tree namespaces"
            "v-expect"
            wait-visible driver {:tag :ul :class "ant-tree-child-tree ant-tree-child-tree-open"})  ; zone for expanded tree 2nd level

      (log! "17. click" "tc-id" "tc-name" "t-s" "test case for view tree namespaces" "v-expect"
            click driver {:tag :span :title "sbrf"})  ; клик на конкретном тексте
      ;; get URL for this resource
      (log! "18. URL " "tc-id" "get URL for this resource" "t-s" "test case for view tree namespaces" "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf"
      ;; get and check element text into breadcrumb line for this resource
      (log! "19. Text is " "tc-id" "get and check element text into breadcrumb line for this resource" "t-s" "test case for view tree namespaces" "sbrf"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[2]/span[1]"}])  ; expect = "sbrf"
      (log! "20. Breadcrumb text is " "tc-id" "get and check element text into breadcrumb line for this resource" "t-s" "test case for view tree namespaces" "true"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[2]/span[1]"}] "sbrf")  ; expect = true

      (log! "21. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver {:tag :span :title "domain"})  ; клик на конкретном тексте
      (log! "22. URL " "tc-id" "tc-name" "t-s" "description" "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/domain"
            get-url driver)  ; expect ="https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/domain"
      (log! "23. Text is " "tc-id" "tc-name" "t-s" "description" "domain"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[3]/span[1]"}])  ; expect = "domain"
      (log! "24. Breadcrumb text is " "tc-id" "tc-name" "t-s" "description" "true"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[3]/span[1]"}] "domain")  ; expect = true

      (log! "25. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver {:tag :span :title "process"})  ; клик на конкретном тексте
      (log! "26. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/process"
      (log! "27. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[3]/span[1]"}])  ; expect = "process"
      (log! "28. Breadcrumb text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[3]/span[1]"}] "process")  ; expect = true

      (log! "29. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver {:tag :span :title "system"})  ; клик на конкретном тексте
      (log! "30. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/system"
      (log! "31. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[3]/span[1]"}])  ; expect = "system"
      (log! "32. Breadcrumb text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[3]/span[1]"}] "system")  ; expect - true

      ;; test case for breadcrumb line
      (log! "33. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[2]/span[1]"}])
      (log! "34. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf"

      ;; test case for breadcrumb line
      (log! "35. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :span :id "breadcrumb__item"}])
      (log! "36. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/"
      (log! "37. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div/span/span[2]"}])  ; expect = ""
      (log! "38. Breadcrumb text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span/span[2]"}] "")  ; expect - true

      ;; test case for text/graph mode
      (log! "39. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "button-meta-mode"}])
      (log! "40. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "mode-container"}])

      (log! "41. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :button :id "button-data-mode"}])  ; expect = "Обычный режим"
      (log! "42. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :button :id "button-data-mode"}] "Обычный режим")  ; expect = true
      (log! "43. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :button :id "button-data-mode"}] "Графический режим")  ; expect = false
      (log! "44. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-css driver [{:tag :button :id "button-data-mode"}] :background-color)  ; expect = "rgba(29, 210, 89, 1)"
      ; checks for displayed picture/shape
      (log! "45. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[3]/span[1]"}])
      (log! "46. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :span :title "pkb"}])
      (log! "46-1. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:xpath ".//*[name()='svg']/*[name()='g']/*[name()='g']"}])  ; expect = false
      (log! "47. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[3]/ul/li[2]/span[1]"}])
      (log! "48. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :span :title "verita"}])
      (log! "49. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :span :title "verita"}])

      (log! "50. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :id "graph-view"}])  ; expect = true
      (log! "51. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :span :id "filter-button"}])  ; expect = true
      ; TODO: or may be screenshot?
      (log! "51-1. Shape model-id: " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:xpath ".//*[name()='svg']/*[name()='g']/*[name()='g']"}] :model-id)
      (log! "51-2. Shape title test: " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[name()='svg']/*[name()='g']/*[name()='g']/*[name()='g']/*[name()='text']"}])
      (log! "51-3. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:xpath ".//*[name()='svg']/*[name()='g']/*[name()='g']"}])  ; expect = true

      ;; test case for use filter configuration in graphical mode
      (log! "52. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-drawer-content"}])  ; expect = false
      (log! "53. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :span :id "filter-button"}])
      (log! "54. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :form :id "meta-drawer-form"}])
      (log! "55. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-drawer-content"}])  ; expect = true
      (log! "56. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :body}])  ; special click driver for close the side panel with configuration of filter
      ;or
      ;(refresh)
      (log! "57. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-drawer-content"}])  ; expect = false

      (log! "58. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "button-data-mode"}])
      (log! "59. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "data-content-container"}])
      (log! "60. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :button :id "button-meta-mode"}])  ; expect = "Графический режим"
      (log! "61. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :button :id "button-meta-mode"}] "Обычный режим")  ; expect = false
      (log! "62. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :button :id "button-meta-mode"}] "Графический режим")  ; expect = true
      (log! "63. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-css driver [{:tag :button :id "button-meta-mode"}] :background-color)  ; expect = "rgba(73, 175, 255, 1)"

      ;; test case for open search panel
      (log! "64. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "search-form"}])  ; expect = false
      (log! "65. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :li :id "nav-search"}])
      (log! "66. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :input :class "search-form__input"}])
      (log! "67. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "search-form"}])  ; expect = true

      ;; test case for search function
      (log! "68. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill-human driver [{:tag :input :class "search-form__input"}] "*process*")
      (wait 3)  ;; wait 3 sec for autostart search function
      (log! "69. Attr is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:tag :input :class "search-form__input"}] :value)  ; expect = "*process*"
      (log! "70. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :id "search-result-dropdown"}])  ; expect = "По Вашему запросу ничего не найдено"
      (log! "71. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :id "search-result-dropdown"}] "По Вашему запросу ничего не найдено")  ; expect = true

      (log! "72. clear" "tc-id" "tc-name" "t-s" "description" "v-expect"
            clear driver [{:tag :input :class "search-form__input"}])
      (log! "73. Attr is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:tag :input :class "search-form__input"}] :value)  ; expect = ""
      (log! "74. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill-human driver [{:tag :input :class "search-form__input"}] "*sbrf.system*")
      (log! "75. Enter" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :input :class "search-form__input"}] k/enter)
      (log! "76. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :id "search-result-dropdown"} {:tag :div :class "ant-collapse-item"}])
      (log! "77. Attr is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:tag :input :class "search-form__input"}] :value)  ; expect = "*sbrf.system*"
      (log! "78. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :id "search-result-dropdown"}] "По Вашему запросу ничего не найдено")  ; expect = false
      (log! "79. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-collapse-header"}])  ; expect = "Пространства имен (...)"
      (log! "80. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-btn-group"} {:tag :a}])  ; expect = "sbrf.system.pkb.verita"

      (log! "81. clear" "tc-id" "tc-name" "t-s" "description" "v-expect"
            clear driver [{:tag :input :class "search-form__input"}])
      (log! "82. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :class "ant-btn search-form__button-close ant-btn-icon-only"}])  ; close the search panel
      (log! "83. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "search-form"}])  ; expect = false

      ;; test case for follow the link in search results
      (log! "84. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :div :class "ant-btn-group"}])
      (log! "85. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver {:tag :main :id "content"})
      ;;; checks for this case
      ;; left side panel is open and entity is highlight
      (log! "86. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :aside :id "main-sider"}])  ; expect = true
      (log! "87. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[3]/ul/li[2]/ul/li[2]/span[2]"}])  ; expect = "verita"
      ;; in browser address field displays full URL to entity
      (log! "88. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/system/pkb/verita"
      ;; into breadcrumbs displayed path to namespace
      (log! "89. Breadcrumb text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[5]/span[1]"}] "verita")  ; expect = true
      ;; in the workspace displays entity specification in textual (ordinary) mode
      (log! "90. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :id "specs-view"}])  ; expect = true
      ;; in the workspace displays visual space for entity icon or shape in graphic mode
      (log! "91. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "button-meta-mode"}])
      (log! "92.wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "data-content-container"}])
      (log! "93. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :button :id "button-data-mode"}])  ; expect = "Обычный режим"
      (log! "94. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :button :id "button-data-mode"}] "Обычный режим")  ; expect = true
      (log! "95. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :id "graph-view"}])  ; expect = true

      ;; test case for follow to the start page
      (log! "96. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :div :id "logo"} {:tag :a} {:tag :span}])
      (log! "97. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :div :id "logo"} {:tag :a}])  ; spin the spinner
      (log! "98. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :input :id "search-form__input"}])

      ;; test case for search function from the start page
      (log! "99. clear" "tc-id" "tc-name" "t-s" "description" "v-expect"
            clear driver [{:tag :input :id "search-form__input"}])
      (log! "100. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :input :id "search-form__input"}] "*process*")
      (wait 3)
      (log! "101. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :id "search-result-dropdown"}])  ; expect = "По Вашему запросу ничего не найдено"
      (log! "102. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :id "search-result-dropdown"}] "По Вашему запросу ничего не найдено")  ; expect = true

      (log! "103. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :class "ant-btn search-form__button-close ant-btn-icon-only"}])  ; close the search panel
      (log! "104. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :div :id "logo"} {:tag :a} {:tag :span}])
      (log! "105. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :input :id "search-form__input"}])
      (log! "106. clear" "tc-id" "tc-name" "t-s" "description" "v-expect"
            clear driver [{:tag :input :id "search-form__input"}])

      (log! "107. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :input :id "search-form__input"}] "*sbrf.system*")
      (log! "108. Enter" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :input :id "search-form__input"}] k/enter)
      (log! "109. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :id "search-result-dropdown"} {:tag :div :class "ant-collapse-item"}])
      (log! "110. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-collapse-header"}])  ; expect = "Пространства имен (...)"
      (log! "111. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-btn-group"} {:tag :a}])  ; expect = "sbrf.system.pkb.verita"

      (log! "112. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :class "ant-btn search-form__button-close ant-btn-icon-only"}])  ; close the search panel
      (log! "112-1. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "menu"}])
; these blocks of testing is not mandatory
;(click driver [{:tag :div :id "logo"} {:tag :a} {:tag :span}])
;(wait-visible driver [{:tag :input :id "search-form__input"}])
;(clear driver [{:tag :input :id "search-form__input"}])

      ;; ---===test suite for authorization user===---
      ;;
      ;; test case for authentication in AuthNZ
      (log! "113. move to" "tc-id" "tc-name" "t-s" "description" "v-expect"
            mouse-move-to driver [{:tag :li :id "user-menu"}])
      (log! "114. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click-visible driver [{:tag :li :id "user-menu__sign-in"}])
      (log! "115. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :input :id :login}])

      (log! "116. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)
      (log! "117. Title " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-title driver)

      (log! "118. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver {:tag :input :id :login :name :login} "verita-admin")
      (log! "119. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver {:tag :input :id :password :name :password} "ext-Secret13")
      (log! "120. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver {:tag :button :class "btn btn-lg btn-block btn-signin"} k/enter)
      (log! "121. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver {:tag :ul :class "menu__nav"})

      (log! "122. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)
      (log! "123. Title " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-title driver)
      ;(log! "123-1. move to..." mouse-move-to driver [{:tag :li :id "usr-menu"}])
      (log! "123-1. move to..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            mouse-move-to driver [{:xpath ".//*[@id='user-menu']"}])
      (wait 2)
      (log! "124. Attr is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:tag :ul :class "ant-menu user-menu__messages ant-menu-light ant-menu-root ant-menu-vertical"} {:tag :li :index 3}] :id)  ; expect = "user-menu__sign-out"
      (log! "125. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver "Для создания нового пространства имен выполните вход в систему")  ; expect = false
      (log! "126. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver "Создать пространство имен")  ; expect = true
      (log! "127.False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :class "ant-card-head-title"}] "Создание нового пространства имен")  ; expect = false
      (log! "128. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :li :id "create-ns"}])
      (log! "129. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "ant-card-body"}])

      (log! "130. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-card-head-title"}])  ; expect= "Создание нового пространства имен"
      (log! "131. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :class "ant-card-head-title"}] "Создание нового пространства имен")  ; expect = true
      (log! "132. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            enabled? driver [{:tag :button :id "create-ns__button"}])  ; expect = false
      (log! "133. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            disabled? driver [{:tag :button :id "create-ns__button"}])  ; expect = true

      (log! "134. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :input :class "ant-input ant-select-search__field"}] "sbrf.domain.kmd")
      (log! "135. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            enabled? driver [{:tag :button :id "create-ns__button"}])  ; expect = false

      (log! "136. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :textarea :id "create-ns__text-area"}] "12")
      (log! "137. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            enabled? driver [{:tag :button :id "create-ns__button"}])  ; expect = false

      (log! "138. clear" "tc-id" "tc-name" "t-s" "description" "v-expect"
            clear driver [{:tag :textarea :id "create-ns__text-area"}])

      ; this step for clearing <textarea> content, because the (clear) function does not update/clearing the contents, can skip
      (log! "139. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :textarea :id "create-ns__text-area"}] " ")
      (log! "140. clear" "tc-id" "tc-name" "t-s" "description" "v-expect"
            clear driver [{:tag :textarea :id "create-ns__text-area"}])

      (log! "141. fill" "tc-id" "tc-name" "t-s" "description" "v-expect"
            fill driver [{:tag :textarea :id "create-ns__text-area"}] "Пространство имен для предметной области КМД")
      (log! "142. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            enabled? driver [{:tag :button :id "create-ns__button"}])  ; expect = true
      (log! "143. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            disabled? driver [{:tag :button :id "create-ns__button"}])  ; expect = false

      (log! "144. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "create-ns__button"}])
      (log! "145. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver {:tag :main :id "content"})
      ;;; checks for this case |
      ;;;----------------------V
      ;; left side panel is open and entity is highlight
      (log! "146. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :aside :id "main-sider"}])  ; expect = true
      (log! "147. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[1]/ul/li/span[2]"}])  ; expect = "kmd"
      ;; in the namespace tree appeared the node with plus symbol
      (log! "148. Attr is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[1]/span[1]"} {:tag :i}] :aria-label)  ; expect= "icon: plus-square"
      ;; in browser address field displays full URL to entity
      (log! "149. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/domain/kmd"
      ;; into breadcrumbs displayed path to namespace
      (log! "150. Breadcrumb full string is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div"}])  ; expect = "/sbrf/domain/kmd"
      (log! "151. Last breadcrumb point is corresponds to the specified? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[4]/span[1]"}] "kmd")  ; expect = true
      ;; in the workspace displays entity specification in textual (ordinary) mode
      (log! "151.True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :id "specs-view"}])  ; expect = true
      ;; in the workspace displays visual space for entity icon or shape in graphic mode
      (log! "152. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "button-meta-mode"}])
      (log! "153. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "data-content-container"}])
      (log! "154. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :button :id "button-data-mode"}])  ; expect = "Обычный режим"
      (log! "155. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :button :id "button-data-mode"}] "Обычный режим")  ; expect = true
      (log! "156. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :id "graph-view"}])  ; expect = true

      (log! "157. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "button-data-mode"}])
      (log! "158. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "data-content-container"}])

      ;; test case for deleting namespace
      (log! "159. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-modal-content"}])  ; expect = false
      (log! "160. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "delete-tool-button"}])
      (log! "161. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "ant-modal-body"}])
      (log! "162. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-modal-content"}])  ; expect = true
      (log! "163. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "confirm-body"}])  ; expect = "Вы хотите удалить пространство имен sbrf.domain.kmd?"
      (log! "164. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :class "confirm-body"}] "Вы хотите удалить пространство имен ")  ; expect = true

      ; check for click driver on Cancel button
      (log! "165. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :class "ant-btn"}])
      (log! "166. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-modal-content"}])  ; expect = false

      (log! "167. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[1]/ul/li/span[2]"}])  ; expect = "kmd"
      (log! "168. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/domain/kmd"
      (log! "169. Breadcrumb full string is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div"}])  ; expect = "/sbrf/domain/kmd"
      (log! "170. Last breadcrumb point is corresponds to the specified? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[4]/span[1]"}] "kmd")  ; expect = true

      ; check for click driver on confirm deleting button
      (log! "171. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-modal-content"}])  ; expect = false
      (log! "172. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :id "delete-tool-button"}])
      (log! "173. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :div :class "ant-modal-body"}])
      (log! "174. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :div :class "ant-modal-content"}])  ; expect = true
      (log! "175. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "confirm-body"}])  ; expect = "Вы хотите удалить пространство имен sbrf.domain.kmd?"
      (log! "176. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :class "confirm-body"}] "Вы хотите удалить пространство имен ")  ; expect = true

      (log! "177. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :button :class "ant-btn ant-btn-danger"}])
      (log! "178. wait..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            wait-visible driver [{:tag :main :id "content"}])

      ; checks for a pop-up window with confirmation text
      (log! "179. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-notification-notice-message"}])  ; expect = "Удаление выполнено"
      (log! "180. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :div :class "ant-notification-notice-description"}])  ; expect = "Пространство имен sbrf.domain.kmd успешно удалено"
      (log! "181. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:tag :div :class "ant-notification-notice-message"}] "Удаление выполнено")  ; expect = true
      (wait 5)
      (log! "181-1. True? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :main :id "content"}])  ; expect = true
      (log! "181-2. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :a :class "ant-notification-notice-close"}])
      (log! "181-3. False? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            visible? driver [{:tag :main :id "content"}])  ; expect = false

      ; checks for the delete specification feature
      (log! "181-4. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :span :class "ant-tree-switcher ant-tree-switcher_close"}])
      (log! "182. Attr is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-attr driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[1]/span[1]"} {:tag :i}] :aria-label)  ; expect= "icon: file"
      (log! "183. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:xpath ".//*[@id='main-sider__not-collapsed-tree']/ul/li/ul/li[1]"} {:tag :span :title "domain"}])
      (log! "184. URL " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-url driver)  ; expect = "https://esa-14.vm.esrt.cloud.sbrf.ru/verita/#/specs/sbrf/domain"
      (log! "185. Breadcrumb full string is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:xpath ".//*[@id='content']/header/div[1]/div"}])  ; expect = "/sbrf/domain"
      (log! "186. Last breadcrumb point is corresponds to the specified? " "tc-id" "tc-name" "t-s" "description" "v-expect"
            has-text? driver [{:xpath ".//*[@id='content']/header/div[1]/div/span[4]/span[1]"}] "kmd")  ; expect = false

      ;; test case for logout function
      ; check to  whether logout from the system (the user is logged in)
      (log! "187. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :li :id "user-menu__sign-out"} {:tag :a}])  ; expect = ""
      (log! "188. move to..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            mouse-move-to driver [{:tag :li :id "user-menu"}])
      (wait 1)
      (log! "189. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :li :id "user-menu__sign-out"} {:tag :a}])  ; expect = "Выйти"

      ; click driver on menu item
      (log! "190. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :li :id "user-menu"}])
      (log! "191. click" "tc-id" "tc-name" "t-s" "description" "v-expect"
            click driver [{:tag :li :id "user-menu__sign-out"}])

      ; checks for confirm exit from the system (the user is not authorized)
      (log! "192. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :li :id "user-menu__sign-in"} {:tag :a}])  ; expect = ""
      (log! "193. move to..." "tc-id" "tc-name" "t-s" "description" "v-expect"
            mouse-move-to driver [{:tag :li :id "user-menu"}])
      (log! "195. Text is " "tc-id" "tc-name" "t-s" "description" "v-expect"
            get-element-text driver [{:tag :li :id "user-menu__sign-in"} {:tag :a}])  ; expect = "Войти"
      driver)
    (catch Exception e
      (spit pm-file (.getMessage e)))
    (finally (spit "output_test.log" (print-res)))))

(comment
  (test-frontend.verita-test-ui/test-ui)
  ; experiment with try
  (try
    (def d (test-frontend.verita-test-ui/test-ui chrome))
    (catch Exception e
      (println "[ERROR] " (.getMessage e))))
  ;
  (def d (test-frontend.verita-test-ui/test-ui chrome))
  (def d (test-frontend.verita-test-ui/test-ui firefox))
  (def d (test-frontend.verita-test-ui/test-ui safari))
  (identity @*res)
  (quit d))
