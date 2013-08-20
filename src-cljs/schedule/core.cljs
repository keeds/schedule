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

(defn set-html
  [el s]
  (aset el "innerHTML" s))

(defn set-class
  [el name]
  (set! (.-className el) name))

(defn clear-class
  [el]
  (set! (.-className el) ""))

(defn add-class
  [el class]
  (.add (.-classList el) class))

(defn remove-class
  [el class]
  (.remove (.-classList el) class))

(defn by-tag-name
  [el tag]
  (.getElementsByTagName el tag))

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

(def mc (:chan (event-chan cells-div "mousemove")))
(def mover (:chan (event-chan cells-div "mouseover")))
(def mout (:chan (event-chan cells-div "mouseout")))
(def kc (:chan (event-chan js/window "keyup")))

(defn mouse-over
  [e]
  (do
    (set-html loc-div "mouseover")
    (add-class (.-target e) "mover")))

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
         [{"x" x "y" y}]    (set-html loc-div (str x ", " y))
         [{"keyCode" code}] (set-html key-div code)
         :else nil))

(def data [[1] [2] [3] [4] [5] [6] [7]])

(defn add-div
  [el val class]
  (let [div (.createElement  js/document "div")
        txt (.createTextNode js/document val)]
    (.add (.-classList div) class)
    (.appendChild div txt)
    (.appendChild el div)))

(doall
 (for [i (range 1 8)]
   (add-div cells-div i "cells")))

(go
 (while true
   (handler (alts! [mover mout mc kc]))))
