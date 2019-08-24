(defproject toyokumo/kintone-clj "0.1.0-SNAPSHOT"
  :description "A kintone SDK for Clojure and ClojureScript"
  :url "https://github.com/toyokumo/kintone-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.500"]
                 [cheshire "5.9.0"]
                 [cljs-ajax "0.8.0"]
                 [clj-http "3.10.0"]]
  :profiles {:provided {:dependencies [[org.clojure/clojurescript "1.10.520"]
                                       [com.bhauman/figwheel-main "0.2.3"]]}
             :dev {:source-paths ["dev"]
                   :resource-paths ["target"]
                   :dependencies [[nrepl/nrepl "0.6.0"]
                                  [cider/piggieback "0.4.0"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
