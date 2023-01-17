build:
  set -e; \
  # Build JAR
  pushd backend; \
  clj -X:uberjar; \
  popd
  # Build CLJS + JS
  pushd frontend; \
  npx shadow-cljs release app; \
  npx webpack --config webpack.prod.js; \
  popd

build-docker:
  docker build -t 'goodplace:latest' .

run-docker:
  docker-compose -f docker-compose.local-test.yml build
  docker-compose -f docker-compose.local-test.yml up

deploy:
  flyctl deploy

check-hash:
  rg '#p\b|#c\b|#l\b' -g '!justfile' || true