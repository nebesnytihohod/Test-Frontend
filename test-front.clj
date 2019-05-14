(ns test-frontend.test-front
  (:require [etaoin.api :refer :all]
            [etaoin.keys :as k]))

(defn test-ui
  "hardcoded test"
  []
  (let [driver (firefox)]
   (go driver "https://habr.com")
   (wait-visible driver [{:class "main-navbar__section main-navbar__section_right"} {:tag :button :type :button}])

   (click driver [{:tag :button :fn/has-class "btn btn_medium btn_navbar_lang js-show_lang_settings"}])

   (click driver [{:tag :span :fn/has-class "checkbox__label checkbox__label_another js-popup_feed_ru"}])
   (click driver [{:tag :button :type :submit :fn/has-class "btn btn_blue btn_huge btn_full-width js-popup_save_btn"}])
   (wait-visible driver {:tag :button :id :search-form-btn :title "Site search"})

   (click driver [{:tag :button :id :search-form-btn :title "Site search"}])
   (fill driver {:tag :input :id :search-form-field} "clojure")
   (fill driver {:tag :input :id :search-form-field} k/enter)
   (wait-visible driver {:tag :ul :class "content-list content-list_posts shortcuts_items"})

   (click driver [{:tag :h2 :class :post__title} {:tag :a :class :post__title_link}])

   (get-url driver)
   (get-title driver)
   (has-text? driver "rocks")
   (screenshot driver "page.png")
   (screenshot-element driver {:tag :div :class "post__text post__text-html js-mediator-article"} "element.png")))

(defn -main
  []
  (test-ui))