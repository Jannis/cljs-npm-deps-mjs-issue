(require '[cljs.build.api :as b])
(require '[cljs.closure])
(require '[clojure.pprint :refer [pprint]])

(b/build "src"
  {:output-dir "out"
   :output-to "out/main.js"
   :optimizations :none
   :verbose true
   :compiler-stats true
   :main 'foo.core
   :install-deps true
   :npm-deps {:iterall "1.2.2"}
   :infer-externs true
   :closure-warnings {:non-standard-jsdoc :off}})
