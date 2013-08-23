(ns schedule.core
  (:refer-clojure :exclude [])
  (:require [schedule.utils  :refer [log by-id add-class remove-class
                                     add-div event-chan]]
            [clojure.string  :refer [join]]
            [cljs.core.async :refer [chan sliding-buffer put!]]
            [dommy.utils     :as utils]
            [dommy.core      :refer [set-html!]]
            [dommy.attrs     :refer [has-class? add-class! remove-class! attr set-attr!]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [clojure.core.match.js :refer [match]]
                   [dommy.macros :refer [sel sel1 node deftemplate]]))

(def keyup     38)
(def keydown   40)
(def keyright  39)
(def keyleft   37)
(def key-s     83)
(def key-h     72)
(def key-t     84)
(def key-space 32)

(def loc (atom {:x 0 :y 0}))

(def pos-div   (sel1 :#pos))
(def loc-div   (sel1 :#location))
(def key-div   (sel1 :#key))
(def cells-div (sel1 :#cells))

(defn cell-id
  [{:keys [x y] :as cell}]
  (when cell
    (str x ":" y)))

(defn pos-watcher
  [_ _ old new]
  (set-html! pos-div (join ", " (vector (:x new) (:y new))))
  (let [old-el (by-id (cell-id old))
        new-el (by-id (cell-id new))]
    (when old-el
      (remove-class old-el "mover"))
    (when new-el
      (add-class new-el "mover"))))

(add-watch loc nil pos-watcher)

(def mc    (:chan (event-chan cells-div "mousemove")))
(def mover (:chan (event-chan cells-div "mouseover")))
(def mout  (:chan (event-chan cells-div "mouseout")))
(def kc    (:chan (event-chan "keyup")))

(defn mouse-over
  [el]
  (when-let [target (.-target el)]
    (when (has-class? target "cell")
      (swap! loc assoc :x (int (attr target :x)) :y (int (attr target :y)))
      (set-html! loc-div "mouseover"))))

(defn mouse-out
  [el]
  (let [target (.-target el)]
    (set-html! loc-div "mouseout")))

(defn key-handler
  [key]
  ;; (log key (type key))
  (cond
   (= key keydown)  (swap! loc assoc :x (inc (:x @loc)))
   (= key keyup)    (swap! loc assoc :x (dec (:x @loc)))
   (= key keyright) (swap! loc assoc :y (inc (:y @loc)))
   (= key keyleft)  (swap! loc assoc :y (dec (:y @loc)))
   :else (let [id   (cell-id @loc)
               cell (by-id id)]
           (cond
            (= key key-s)     (set-html! cell "Sick")
            (= key key-h)     (set-html! cell "Holiday")
            (= key key-t)     (set-html! cell "Training")
            (= key key-space) (set-html! cell "")))))

(defn handler
  [[e c]]
  (match [e]
         [{"type" "mouseover"}] (mouse-over e)
         [{"type" "mouseout"}]  (mouse-out e)
         [{"x" x "y" y}]        (set-html! loc-div (str x ", " y))
         [{"keyCode" code}]     (do
                                  (set-html! key-div code)
                                  (key-handler code))
         :else nil))

(def data
  [["Bill" "001234" "000022" "" "" "" "" ""]
   ["Ben" "Sick" "Holiday" "Training" "" "000666" "" ""]
   ["Bob" "" "" "" "" "" "" ""]])

(doseq [[x row] (map-indexed vector data)]
  (let [div (add-div cells-div "" nil "row")]
    (add-div div (first row) nil "name")
    (doseq [[y cell] (map-indexed vector (rest row))]
      (-> (add-div div cell (str x ":" y) "cell")
          (set-attr! :x x :y y)))))

(go
 (while true
   (handler (alts! [mover mout mc kc]))))
