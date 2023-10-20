(ns user
  (:require
   [figwheel.main.api :as fig]))

(defn fig-start []
  (fig/start "dev"))

(defn fig-stop []
  (fig/stop "dev"))

(defn cljs-repl []
  (fig/cljs-repl "dev"))
