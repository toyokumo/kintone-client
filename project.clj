(defproject toyokumo/kintone-client "0.6.1"
  :description "A kintone client for Clojure and ClojureScript"
  :url "https://github.com/toyokumo/kintone-client"
  :license {:name "Apache, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [org.clojure/core.async "1.6.681"]
                 [clj-http "3.12.3"]
                 [cheshire "5.13.0"]]
  :clean-targets ^{:protect false} ["target"]
  :aliases {"test:cljs" ["doo" "chrome-headless" "test" "once"]
            "bump-version" ["change" "version" "leiningen.release/bump-version"]}
  :plugins [[lein-doo "0.1.11"]]
  :doo {:paths {:karma "node_modules/karma/bin/karma"}}
  ;; Switch to Figwheel Main when this issue is solved
  ;; https://github.com/bhauman/figwheel-main/issues/159
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test" "script"]
                        :compiler {:output-to "target/public/js/test.js"
                                   :main cljs.test-runner
                                   :optimizations :none}}]}
  :profiles {:provided {:dependencies [[org.clojure/clojurescript "1.11.132"]
                                       [cljs-ajax "0.8.4"]]}
             :dev {:source-paths ["dev"]
                   :resource-paths ["target"]
                   :dependencies [[nrepl/nrepl "1.1.1"]
                                  [cider/piggieback "0.5.3"]
                                  [com.bhauman/figwheel-main "0.2.18"]
                                  [com.bhauman/cljs-test-display "0.1.1"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
