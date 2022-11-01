# GoodPlace

A rough project based on some learnings from pingcrm from PrestanceDesign but
with ChakraUI as the UI framework.

Leaning away from closure compiler for JS assets as it grows more incompatible
and lags further behind.

## Todo

- [x] Add easy running of cljs, js parts
- [x] Add authentication for routes
- [x] Add basic tests
- [x] Add user management
- [x] Reimplement cljs friendly hooks for inertia
- [x] Fork out inertia wrapping into new library
- [x] Add titles to pages
- [x] Produce jar of application
- [x] Deployment (fly.io)
- [x] Improve error feedback development time
- [x] Add local postgres
- [x] Separate handlers into namespaces
- [x] Use postgres for notes too
- [x] Remove old sqlite implementations
- [ ] Fix broken symbols in prod
- [ ] Use production postgres
- [ ] Add rationale for GoodPlace
- [ ] Add authorization for routes
- [ ] Do more to split development time dependencies
- [ ] CI tests (Github Actions)
- [ ] Improve tests
- [ ] Improve appearance of side menu
- [ ] Add individual pages for server errors
- [ ] Add components to wrap inertia links, buttons
- [ ] Separate pages into namespaces

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

