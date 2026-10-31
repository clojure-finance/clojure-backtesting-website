{:title "Release Notes"
 :date "2026-10-31"
 :layout :post
 :section "Help"
 :lead "What changed in each version of the library, and what to check when you upgrade."
 :toc true}

The library is published on [Clojars](https://clojars.org/com.github.clojure-finance/clojure-backtesting) since version 1.1.2; see [Get Started](/posts/get-started/#install) for how to add it to a project. The full history is in the [repository](https://github.com/clojure-finance/clojure-backtesting/commits/master).

## 1.1.3

*2026-10-31.* Fixes for the Yahoo Finance data source.

- `latest-earnings` and `days-since-earnings` return `nil` for a security without fundamentals, instead of throwing.
- `yahoo-dataset!` with `:out-dir` now loads the dataset it built. It used to load the input directory.
- An exchange rate series comes from the latest download that has prices, as securities already did, so a failed download of a rate no longer hides an earlier good one.
- A `:start` and `:end` range without any prices is an error, instead of writing an empty dataset.
- The [value strategy](/posts/examples/example-value-strategy/) worksheet sets its commission back afterwards, so the worksheets' outputs no longer depend on the order they run in.

## 1.1.2

*2026-10-31.* Real data, fundamentals and portfolio strategies.

**On Clojars.** `com.github.clojure-finance/clojure-backtesting` can now be added to any Leiningen or Clojure CLI project. The jar holds the sources and the sample data, and no longer any compiled classes.

**Yahoo Finance data.** [`download-yahoo!`](/posts/yahoo-data/#download-yahoo) downloads prices, dividends, splits, financial statements and earnings dates for any ticker, through [clj-yfinance](https://github.com/clojure-finance/clj-yfinance), with no API key. [`build-yahoo-dataset!`](/posts/yahoo-data/#build-yahoo-dataset) turns the downloads into a CRSP-style dataset with prices as traded, and [`load-yahoo-dataset`](/posts/yahoo-data/#load-yahoo-dataset) loads it. Downloads are resumable and accumulate, and the build works offline. Synthetic snapshots in `resources/sample-yahoo` let you try it without a network connection. See [Yahoo Finance Data](/posts/yahoo-data/).

**Point-in-time fundamentals.** The new namespace `clojure-backtesting.fundamentals` returns statement values as they were known on the backtest date, per share of that day and in the trading currency, with twelve-month figures, history, and ratios: market cap, P/E, earnings yield, book-to-market, EV/EBITDA, dividend yield, margins, returns on equity and assets, growth and the Piotroski F-score. Earnings announcements come with their surprise. `FUNDAMENTALS-MAX-AGE-MONTHS` (18) limits how old a value may be. See [Fundamentals](/posts/fundamentals/).

**Strategy helpers.** The new namespace `clojure-backtesting.strategy` has `new-period?`, `rank-by`, `top-n`, `equal-weights`, `order-target-percent` and `rebalance`. See [Strategy Helpers](/posts/strategy/).

**Benchmark comparison.** [`benchmark-stats`](/posts/evaluate/#benchmark-stats) compares a run with any security in the dataset: returns, beta, alpha, correlation, tracking error and information ratio.

**New examples.** `yahoo_finance_data`, `value_strategy` and `quality_and_earnings`, all running on the synthetic snapshots.

**Fix.** Loading a second main dataset, over other dates, could make a full data cache throw a `NullPointerException`, because the cache still held the old dataset's dates. It is now cleared.

**Upgrading.** The REPL's `user` namespace refers the three new namespaces. In your own namespaces, add `clojure-backtesting.fundamentals`, `clojure-backtesting.strategy` and `clojure-backtesting.yahoo` to the `:require` list if you use them. `write-grouped-dataset` moved from `clojure-backtesting.sample-data` to `clojure-backtesting.data`.

## 1.1.1

*2026-10-25.* Correctness fixes. **The outputs of active strategies change**, mostly their volatility and Sharpe ratio, so rerun results you compare against.

- **Daily returns on fill days.** On every day an order filled, the daily return was about 0, which made volatility and the Sharpe ratio wrong for strategies that trade often. It is now measured from the previous day's value.
- **MACD signal line.** It was an EMA of the price; it is now the 9-day EMA of the MACD line, as defined. `MACD` therefore returns `nil` until the 34th close with the default cycles.
- **Gaps in prices.** EMA, MACD and RSI hold their value through days without a price and take one step when trading resumes. They used to restart, or return `nil`.
- **Restarting.** `init-portfolio` drops orders still pending from the previous run; they used to fill in the new one.
- **Profit per trade** counts only the strategy's orders, not the ones `end-order` places to close positions, which roughly halved it in the final report row.
- The formatted maximum drawdown ends in `%`, like the volatility. `add-aprc` prints one progress line per year.

## 1.1.0

*2026-09-14.* A rewrite of the core, tested end to end, and the bundled sample dataset.

- **Dollar accounting.** Cash and holdings are valued in dollars at the traded price, with commissions on the dollar value. Dividends are reinvested by default, or paid to cash with `(update-reinvest-dividends false)`.
- **Margin for short sales.** Short positions count as borrowing: they must meet the initial margin, and the maintenance margin check closes them. `portfolio-exposure` summarises cash, long and short value, equity, gross position and loan, and the portfolio record has a `:short` column.
- **Trading rules.** On each day, orders that reduce a position fill before those that add to one. Holdings without a price for `MISSING-DAYS-LIMIT` (10) trading days are booked as cash. Order and automation expiry count trading days.
- **Compustat join.** Each day uses, per security, the latest filing whose period end and report date (`rdq`) had both passed, up to `MERGE-MAX-AGE-MONTHS` (6) old.
- **Indicators.** RSI smoothing, the ATR's true range and `parabolic-SAR` (now Wilder's indicator, with a state map) are fixed; `moving-avg` and `moving-sd` return `nil` without a full window.
- **Output files** are written only when `update-output-dir` sets a directory.
- **Sample dataset.** `resources/sample-data` holds three made-up securities over January to March 1990, with a dividend, a split and a gap, plus Compustat-style filings, so every example runs without licensed data. The example worksheets were rewritten for it.
- **Removed:** Jupyter support (replaced by Gorilla REPL), dead code and several unused dependencies.

## 1.0

*2021-03-13.* The first release, with the day-stepping engine, the CRSP and Compustat data layout, technical indicators, automation, the evaluation report and plotting.
