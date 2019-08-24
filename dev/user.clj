(ns user
  (:require [figwheel.main.api :as fig]))

(defn fig-start []
  (fig/start {:mode :serve}
             {:id "dev"
              :options {:main 'kintone.dev
                        :optimizations :none}
              :config {:watch-dirs ["src" "dev"]}}))

(defn fig-stop []
  (fig/stop "dev"))

(defn cljs-repl []
  (fig/cljs-repl "dev"))
