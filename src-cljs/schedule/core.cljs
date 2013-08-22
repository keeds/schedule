(ns schedule.core
  (:refer-clojure :exclude [])
  (:require [schedule.utils  :refer [log add-div event-chan]]
            [clojure.string  :refer [join]]
            [cljs.core.async :refer [chan sliding-buffer put!]]
            [dommy.utils     :as utils]
            [dommy.core      :refer [set-html!]]
            [dommy.attrs     :refer [has-class? add-class! remove-class! attr set-attr!]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [clojure.core.match.js :refer [match]]
                   [dommy.macros :refer [sel sel1 node deftemplate]]))

(def loc (atom {:x 0 :y 0}))

(def pos-div   (sel1 :#pos))
(def loc-div   (sel1 :#location))
(def key-div   (sel1 :#key))
(def cells-div (sel1 :#cells))

(def mc    (:chan (event-chan cells-div "mousemove")))
(def mover (:chan (event-chan cells-div "mouseover")))
(def mout  (:chan (event-chan cells-div "mouseout")))
(def kc    (:chan (event-chan "keyup")))

(defn mouse-over
  [el]
  (let [target (.-target el)]
    (set-html! pos-div (join ", " (vector (attr target :x) (attr target :y))))
    (set-html! loc-div "mouseover")
    (when (has-class? target "cell")
      (add-class! target "mover"))))

(defn mouse-out
  [el]
  (let [target (.-target el)]
    (set-html! loc-div "mouseout")
    (when (has-class? target "cell")
      (remove-class! target "mover"))))

(defn handler
  [[e c]]
  ;; (log "handler" (type e) e)
  (match [e]
         [{"type" "mouseover"}] (mouse-over e)
         [{"type" "mouseout"}]  (mouse-out e)
         [{"x" x "y" y}]        (set-html! loc-div (str x ", " y))
         [{"keyCode" code}]     (set-html! key-div code)
         :else nil))

(def data
  [["Bill" "1" "2" "3" "4" "5" "6" "7"]
   ["Ben" "Sick" "Holiday" "Training" "" "000666" "6" "7"]
   ["Bob" "1" "2" "a" "A" "" "" ""]])

(doseq [[x row] (map-indexed vector data)]
  (let [div (add-div cells-div "" nil "row")]
    (add-div div (first row) nil "name")
    (doseq [[y cell] (map-indexed vector (rest row))]
      (-> (add-div div cell (str x ":" y) "cell")
          (set-attr! :x x :y y)))))

(go
 (while true
   (handler (alts! [mover mout mc kc]))))
