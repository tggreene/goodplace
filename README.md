# GoodPlace

A rough project based on some learnings from pingcrm from PrestanceDesign but
with ChakraUI as the UI framework.

Leaning away from closure compiler for JS assets as it grows more incompatible
and lags further behind.

## Todo

- [ ] Add easy running of cljs, js parts
- [ ] Add components to wrap inertia links, buttons
- [ ] Reimplement cljs friendly hooks for inertia

## Adding a new page or route

1) Add it to `goodplace.shared.routes`
2) Depends on the type of page or route
    * For just a page you need to
      1) add a page component, maybe in `goodplace.pages`
      2) add a join to your new component via `page-implementations` in `goodplace.app`'
      3) add an entry to `route-implementations` for the same id, it can just
      use `(inertia-handler <id>)`
    * For a page and a route you can follow the steps for a page but add
      different handling on the different methods
3) Inertia caches components so you'll need to refresh to see changes

