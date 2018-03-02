# cljs-npm-deps-mjs-issue

This repo provides a simple way to reproduce a bug in ClojureScript
that causes JS projects that use `.mjs` extensions for ES6 modules
to not be detected correctly.

## Steps to reproduce

1. Run the following command to build `out/main.js`:
   ```sh
   clj -Sdeps '{:deps {org.clojure/clojurescript {:mvn/version "1.10.64"}}}' \
       -i build.clj
   ```
2. Open `index.html` in the browser (e.g. `open index.html`)
   * Open the browser devtools
   * Refresh the page

## Expected result

The devtools JS console logs the `iterall` module.

## Actual result

The devtools JS console logs the following error:

```java
base.js:1357 Uncaught Error: Undefined nameToPath for iterall
    at visitNode (base.js:1357)
    at Object.goog.writeScripts_ (base.js:1369)
    at Object.goog.require (base.js:706)
    at index.html:7
```

## Observations

### out/cljs_deps.js

This file should contain `goog.addDependency` entries for the `iterall` package.
Instead, all it contains is this:

```js
goog.addDependency("base.js", ['goog'], []);
goog.addDependency("../cljs/core.js", ['cljs.core'], ['goog.string', 'goog.Uri', 'goog.object', 'goog.math.Integer', 'goog.string.StringBuffer', 'goog.array', 'goog.math.Long']);
goog.addDependency("../process/env.js", ['process.env'], ['cljs.core']);
goog.addDependency("../foo/core.js", ['foo.core'], ['cljs.core', 'iterall']);
```

Given this, it's no surprise that `main.js` fails to resolve `iterall`.

### Node module indexing

The following command prints the output of ClojureScript's Node module indexing
(thanks @swannodette) and greps for `iterall`:
```sh
clj -Sdeps '{:deps {org.clojure/clojurescript {:mvn/version "1.10.64"}}}' \
    -e "(require '[cljs.closure :as cc] '[clojure.pprint :refer [pprint]]) (pprint (cc/index-node-modules-dir))" \
  | grep -C3 iterall
```

The output is:
```json
  :module-type :es6,
  :provides ["tapable/lib/Tapable.js" "tapable/lib/Tapable" "tapable"]}
 {:file
  "/Users/jannis/Work/oss/jannis/cljs-npm-deps-mjs-issue/node_modules/iterall/index.js",
  :module-type :es6,
--
  :module-type :es6,
  :provides ["iterall/index.js" "iterall/index" "iterall"]}
 {:file
--
 {:file
  "/Users/jannis/Work/oss/jannis/cljs-npm-deps-mjs-issue/node_modules/iterall/package.json",
  :module-type :es6}
  ```

The first match is interesting: `node_modules/iterall/index.js` does not exist.
What exists instead os `node_modules/iterall/index.mjs`.
