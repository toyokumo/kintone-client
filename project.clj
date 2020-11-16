(defproject toyokumo/kintone-client "0.6.0-SNAPSHOT"
  :description "A kintone client for Clojure and ClojureScript"
  :url "https://github.com/toyokumo/kintone-client"
  :license {:name "Apache, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :deploy-repositories [["releases" {:url "https://repo.clojars.org" :creds :gpg}]
                        ["snapshots" :clojars]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.500"]
                 [clj-http "3.10.1"]
                 [cheshire "5.9.0"]]
  :clean-targets ^{:protect false} ["target"]
  :aliases {"test:cljs" ["doo" "chrome-headless" "test" "once"]
            "bump-version" ["change" "version" "leiningen.release/bump-version"]}
  :plugins [[lein-doo "0.1.10"]]
  ;; Switch to Figwheel Main when this issue is solved
  ;; https://github.com/bhauman/figwheel-main/issues/159
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test" "script"]
                        :compiler {:output-to "target/public/js/test.js"
                                   :main cljs.test-runner
                                   :optimizations :none}}]}
  :profiles {:provided {:dependencies [[org.clojure/clojurescript "1.10.520"]
                                       [cljs-ajax "0.8.0"]]}
             :dev {:source-paths ["dev"]
                   :resource-paths ["target"]
                   :dependencies [[nrepl/nrepl "0.6.0"]
                                  [cider/piggieback "0.4.0"]
                                  [com.bhauman/figwheel-main "0.2.3"]
                                  [com.bhauman/cljs-test-display "0.1.1"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
