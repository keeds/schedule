(ns schedule.core
  (:refer-clojure :exclude [])
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [chan sliding-buffer put!]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [clojure.core.match.js :refer [match]]))

(extend-type object
  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))
  (-lookup [coll k not-found]
    (if (.hasOwnProperty coll k)
      (aget coll k)
      not-found)))

(defn log
  [& args]
  (.log js/console (join ", " args)))

(defn by-id
  [id]
  (.getElementById js/document id))

(defn by-tag-name
  [el tag]
  (.getElementsByTagName el tag))

(defn set-html
  [el s]
  (aset el "innerHTML" s))

(defn add-class
  [el class]
  (.add (.-classList el) class))

(defn remove-class
  [el class]
  (.remove (.-classList el) class))

(defn add-div
  [el val id class]
  (let [div (.createElement  js/document "div")
        txt (.createTextNode js/document val)]
    (when-not (nil? id)
      (aset div "id" id))
    (when-not (nil? class)
      (.add (.-classList div) class))
    (.appendChild div txt)
    (.appendChild el div)
    div))

(defn event-chan
  ([type] (event-chan js/window type))
  ([el type] (event-chan (chan) el type))
  ([c el type]
     (log "event-chan" c el type)
     (let [writer #(put! c %)]
       (.addEventListener el type writer)
       {:chan c
        :unsubscribe #(.removeEventListener el type writer)})))

(def loc-div   (by-id "location"))
(def key-div   (by-id "key"))
(def cells-div (by-id "cells"))

(def mc    (:chan (event-chan cells-div "mousemove")))
(def mover (:chan (event-chan cells-div "mouseover")))
(def mout  (:chan (event-chan cells-div "mouseout")))
(def kc    (:chan (event-chan "keyup")))

(defn mouse-over
  [e]
  (do
    (set-html loc-div "mouseover")
    (when (.contains (.-classList (.-target e)) "cell")
      (add-class (.-target e) "mover"))))

(defn mouse-out
  [e]
  (do
    (set-html loc-div "mouseout")
    (remove-class (.-target e) "mover")))

(defn handler
  [[e c]]
  (log "handler" (type e) e)
  (match [e]
         [{"type" "mouseover"}] (mouse-over e)
         [{"type" "mouseout"}]  (mouse-out e)
         [{"x" x "y" y}]        (set-html loc-div (str x ", " y))
         [{"keyCode" code}]     (set-html key-div code)
         :else nil))

;; (doall
;;  (for [row (range 1 21)]
;;    (let [div (add-div cells-div "" nil "row")]
;;      (add-div div "{name}" nil "name")
;;      (doall
;;       (for [i (range 1 8)]
;;         (add-div div i (str row i) "cell"))))))

(def data
  [["Bill" "1" "2" "3" "4" "5" "6" "7"]
   ["Ben" "Sick" "Holiday" "Training" "" "000666" "6" "7"]])

(doall
 (for [row data]
   (let [div (add-div cells-div "" nil "row")]
     (add-div div (first row) nil "name")
     (doall
      (for [x (rest row)]
        (add-div div x (str row x) "cell"))))))

(go
 (while true
   (handler (alts! [mover mout mc kc]))))
