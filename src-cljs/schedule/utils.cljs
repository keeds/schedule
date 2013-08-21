(ns schedule.utils
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

(defn add-class-in-class
  [el class in-class]
  (when (.contains (.-classList el) in-class)
    (add-class el class)))

(defn remove-class-in-class
  [el class in-class]
  (when (.contains (.-classList el) in-class)
    (remove-class el class)))

;; (defn mouse-over
;;   [e]
;;   (do
;;     (set-html loc-div "mouseover")
;;     (when (.contains (.-classList (.-target e)) "cell")
;;       (add-class (.-target e) "mover"))))

;; (defn mouse-out
;;   [e]
;;   (do
;;     (set-html loc-div "mouseout")
;;     (remove-class (.-target e) "mover")))
