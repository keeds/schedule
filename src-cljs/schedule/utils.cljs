(ns schedule.utils
  (:refer-clojure :exclude [])
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [chan sliding-buffer put!]]))

(def ENTER       13)
(def UP_ARROW    38)
(def DOWN_ARROW  40)
(def RIGHT_ARROW 39)
(def LEFT_ARROW  37)
(def S_KEY       83)
(def H_KEY       72)
(def T_KEY       84)
(def SPACE       32)
(def TAB          9)
(def ESC         27)

(def KEYS #{ENTER UP_ARROW DOWN_ARROW RIGHT_ARROW LEFT_ARROW
            S_KEY H_KEY T_KEY SPACE TAB ESC})

(defn key-event->keycode
  [e]
  (.-keyCode e))

(defn key->keyword
  [code]
  (condp = code
    UP_ARROW    :up
    DOWN_ARROW  :down
    LEFT_ARROW  :left
    RIGHT_ARROW :right
    S_KEY       :sick
    H_KEY       :holiday
    T_KEY       :training
    SPACE       :clear))


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

