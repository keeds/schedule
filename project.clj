(defproject schedule "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1859"]
                 [org.clojure/core.async "0.1.222.0-83d0c2-alpha"]
                 [org.clojure/core.match "0.2.0-rc6"]
                 [prismatic/dommy "0.1.2"]
                 [com.keminglabs/reflex "0.1.1"]]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :cljsbuild {
              :builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to     "schedule.js"
                                   :optimizations :whitespace
                                   :pretty-print  true}}]}
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.4"]]
                   :plugins [[lein-cljsbuild "0.3.3"]
                             [com.cemerick/austin "0.1.1"]]}})
