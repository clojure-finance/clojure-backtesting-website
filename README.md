# Clojure Backtesting Documentation Website

The documentation site for [Clojure Backtesting](https://github.com/clojure-finance/clojure-backtesting),
built with [Cryogen](http://cryogenweb.org).

Live site: <https://clojure-finance.github.io/clojure-backtesting-website/>

## Requirements

- Java 11 or later
- [Leiningen](https://leiningen.org/)

## Preview

```bash
lein serve
```

Then open <http://localhost:3000/clojure-backtesting-website/>. The server
rebuilds the site when files in `content/` or `themes/` change.

## Build

```bash
lein run
```

The site is written to `public/clojure-backtesting-website/`.

## Deploy

GitHub Pages serves the `gh-pages` branch, which is pushed by hand: pushing
`master` does not deploy. Build with `lein run`, then commit the contents of
`public/clojure-backtesting-website/` as the whole tree of `gh-pages` and push:

```bash
lein run
git worktree add /tmp/gh-pages gh-pages
rsync -a --delete --exclude .git public/clojure-backtesting-website/ /tmp/gh-pages/
cd /tmp/gh-pages && git add -A && git commit -m "Deploy" && git push origin gh-pages
cd - && git worktree remove /tmp/gh-pages
```

## Layout

| Path | Contents |
| ---- | -------- |
| `content/config.edn` | Site settings, including `:library-version`, shown in the header and on the home page |
| `content/md/pages/` | The home page (`index.md`) and the About page |
| `content/md/posts/` | The documentation pages; `examples/` holds the example walkthroughs |
| `content/img/` | Images and diagrams |
| `content/posts/docs/` | CRSP and Compustat column definitions (PDF) |
| `themes/backtest/` | The theme: templates in `html/`, all styles in `css/style.css`, behaviour in `js/site.js` |

The sidebar navigation is written out in `themes/backtest/html/base.html`; add a
link there when you add a page. The previous/next links at the bottom of each
page follow the sidebar order.

## Writing pages

Each page starts with an EDN map. Besides `:title`, `:date` and `:layout`,
the theme uses:

- `:section`, the small label above the title, such as `"API Reference"`
- `:lead`, a one-sentence summary under the title, also used as the page description
- `:toc true`, the "On this page" contents, from the `##`, `###` and `####` headings

Conventions the theme styles:

- Fenced code with the `clojure` language is highlighted and gets a copy button;
  `bash` blocks are labelled "Terminal"; `text` blocks and blocks without a
  language are labelled "Output".
- A blockquote is a note. Start it with `**Tip:**` for a green tip, or
  `**Watch out:**` for an amber warning.
- Link to other pages with root-relative paths ending in `/`, such as
  `/posts/api/#order`; the build adds the `/clojure-backtesting-website` prefix.
- Inside raw HTML, an `<a>` may only contain inline elements: the build's HTML
  parser closes it before an `<h3>` or `<p>`. Cards use
  `<span class="card-title">` and `<span class="card-text">` instead.

The example outputs on the site come from real runs on the library's bundled
sample dataset. Rerun them when the library changes, and update
`:library-version`. The home page's numbers and its chart
(`content/img/example-backtest.svg`) come from running the strategy shown in
its hero, in `themes/backtest/html/home.html`; the order record excerpt and
stats are in `content/md/pages/index.md`.
