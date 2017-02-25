(defproject darkleaf/router "0.3.0"
  :description "Bidirectional Ring router. REST oriented."
  :url "https://github.com/darkleaf/router"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.293" :scope "provided"]
                 [uritemplate-clj "1.1.1" :scope "test"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-doo "0.1.7"]]
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["test" "src"]
                        :compiler {:optimizations :none
                                   :target :nodejs
                                   :output-to "target/testable.js"
                                   :output-dir "target"
                                   :main darkleaf.test-runner}}]})
