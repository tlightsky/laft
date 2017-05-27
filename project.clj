(defproject laft "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/core.async "0.3.442"]
                 [seesaw "1.4.5"]
                 [com.weblookandfeel/weblaf-core "1.2.8"]
                 [com.weblookandfeel/weblaf-ui "1.2.8"]
                 [clojure-watch "LATEST"]
                 [me.raynes/fs "1.4.6"]]
  :main ^:skip-aot laft.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
