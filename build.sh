#!/usr/bin/env bash

set -e

# Build JAR
pushd backend
clj -X:uberjar
popd

# Build CLJS + JS
pushd frontend
npx shadow-cljs release app
npx webpack --config webpack.prod.js
popd
