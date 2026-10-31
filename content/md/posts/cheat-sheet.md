{:title "Cheat Sheet"
 :date "2026-10-25"
 :layout :post
 :section "API Reference"
 :lead "Every function of the library on one page, grouped by what you want to do. Follow a link for the arguments, return values and examples."
 :toc true}

In `lein repl` all of these are available right away. In your own namespace, require them as shown in [Get Started](/posts/get-started/#use-the-library-in-your-own-namespace). Arguments in square brackets are optional.

## A typical backtest

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc) ; 1. load the data
(init-portfolio "1990-01-02" 10000)                          ; 2. start a portfolio

(while (< (compare (get-date) "1990-03-29") 0)               ; 3. every trading day:
  (let [price (get-permno-price "10001")
        ma20  (moving-avg "10001" 20)]
    (when (and price ma20)
      ;; hold 50 shares while the price is below its 20-day average
      (order "10001" (if (< price ma20) 50 0) :remaining true)))
  (update-eval-report)
  (next-date))

(end-order)                                                  ; 4. close positions
(print-eval-report)                                          ; 5. review
```

## A typical rebalancing backtest

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")                  ; 1. prices and fundamentals
(init-portfolio "2023-01-03" 100000)                        ; 2. start a portfolio

(while (neg? (compare (get-date) "2024-12-31"))            ; 3. every trading day:
  (when (new-period? :month)                                ;    on the first of the month,
    (rebalance (equal-weights (top-n earnings-yield 2))))   ;    hold the two cheapest
  (next-date)
  (update-eval-report))

(end-order)                                                 ; 4. close positions
(benchmark-stats "IDX")                                     ; 5. compare with the index
```

## Set up a run

| Function | What it does |
| -------- | ------------ |
| [`(load-dataset dir role [add-aprc])`](/posts/api/#load-dataset) | Load the `"main"` price dataset or a `"compustat"` fundamentals dataset |
| [`(load-yahoo-dataset dir)`](/posts/yahoo-data/#load-yahoo-dataset) | Load a dataset built from Yahoo Finance: prices and fundamentals |
| [`(init-portfolio date capital)`](/posts/api/#init-portfolio) | Start (or restart) with `capital` in cash on `date` |
| [`(update-output-dir dir)`](/posts/parameters/#update-output-dir) | Also write the records as CSV files; call before `init-portfolio` |
| [`(CHANGE-CACHE-SIZE n)`](/posts/user-guide/#cache-mechanism) | Keep `n` days of data in memory (60 by default) |

## Move through time

| Function | What it does |
| -------- | ------------ |
| [`(get-date)`](/posts/api/#get-date) | Today's date, as `"YYYY-MM-DD"` |
| [`(next-date)`](/posts/api/#next-date) | Move to the next trading day, fill pending orders, run automations. `nil` once the run has ended |
| [`(get-next-date)`](/posts/api/#get-next-date) | The next trading day, without moving |
| [`(get-prev-n-date n)`](/posts/api/#get-prev-n-date) | The date `n` trading days ago |
| [`(date-after-n-trading-days date n)`](/posts/api/#date-after-n-trading-days) | The date `n` trading days after `date` |

## Read market data

| Function | What it does |
| -------- | ------------ |
| [`(available-permnos)`](/posts/api/#available-permnos) | The securities that trade today |
| [`(get-info)`](/posts/api/#get-info) | Today's rows, one map per security |
| [`(get-info-map)`](/posts/api/#get-info-map) | Today's rows, as a map from permno to row |
| [`(get-permno-info [date] permno)`](/posts/api/#get-permno-info) | One security's row |
| [`(get-permno-price [date] permno)`](/posts/api/#get-permno-price) | One security's trading price |
| [`(get-permno-by-key [date] permno key)`](/posts/api/#get-permno-by-key) | One column of one security's row, such as `:TICKER` |
| [`(get-prev-n-days [n])`](/posts/api/#get-prev-n-days) | The rows of the previous `n` trading days, most recent first |
| [`(get-prev-n-days-map [n])`](/posts/api/#get-prev-n-days-map) | The same, one permno-keyed map per day |
| [`(get-permno-prev-n-days permno n)`](/posts/api/#get-permno-prev-n-days) | One security's rows over the previous `n` trading days |
| [`(permno-tic permno)`, `(tic-permno ticker)`](/posts/api/#permno-tic-and-tic-permno) | Translate between permnos and tickers |
| `(->double x)` | Convert a Compustat value (a string) to a number; see the [Fundamental Analysis](/posts/examples/example-fundamental-analysis/) example |

## Trade

| Function | What it does |
| -------- | ------------ |
| [`(order permno quantity & options)`](/posts/api/#order) | Buy (positive) or sell (negative) shares; fills at the next close |
| `(order permno n :remaining true)` | Trade until you hold `n` shares; `0` closes the position |
| `(order ... :leverage false)` | Fail instead of borrowing cash or shares |
| `(order ... :expiration n)` | Keep the order pending for up to `n` trading days |
| `(order ... :print true)` | Print the order when it fills |
| [`(end-order)`](/posts/api/#end-order) | Close every position at today's close and end the run |
| `@pending-order` | Orders that have not filled yet |

## Rank and rebalance

| Function | What it does |
| -------- | ------------ |
| [`(new-period? unit)`](/posts/strategy/#new-period) | True on the first trading day of a `:week`, `:month`, `:quarter` or `:year`, and on the first day of the run |
| [`(rank-by f & options)`](/posts/strategy/#rank-by) | `[[permno value] ...]`, highest `(f permno)` first; `:universe`, `:ascending` |
| [`(top-n f n & options)`](/posts/strategy/#top-n) | The `n` permnos with the highest `(f permno)` |
| [`(equal-weights permnos)`](/posts/strategy/#equal-weights) | `{permno 1/n}` |
| [`(order-target-percent permno weight)`](/posts/strategy/#order-target-percent) | Order so the holding becomes `weight` of equity; `:whole-shares? true` |
| [`(rebalance weights)`](/posts/strategy/#rebalance) | Target `{permno weight}` for the whole portfolio, closing everything else |
| [`(portfolio-equity)`](/posts/strategy/#portfolio-equity-and-target-quantity) | Equity as last valued |

## Inspect the portfolio

| Function | What it does |
| -------- | ------------ |
| [`(print-portfolio)`](/posts/portfolio/#print-portfolio) | Current holdings and cash |
| [`(print-portfolio-record [n])`](/posts/portfolio/#print-portfolio-record) | Daily value, return, loan, leverage and margin; `-1` prints every row |
| [`(print-order-record [n])`](/posts/portfolio/#print-order-record) | Filled orders |
| [`(portfolio-exposure)`](/posts/portfolio/#portfolio-exposure) | Cash, long, short, equity, gross and loan, in dollars |
| [`@portfolio`, `@portfolio-value`, `@order-record`](/posts/portfolio/#raw-records) | The same data as Clojure values, for your own analysis |

## Measure performance

| Function | What it does |
| -------- | ------------ |
| [`(update-eval-report)`](/posts/evaluate/#update-eval-report) | Add today's metrics to the evaluation report |
| [`(print-eval-report [n])`](/posts/evaluate/#print-eval-report) | Value, volatility, Sharpe ratio, profit per trade and drawdown, per day |
| `@eval-record` | The report as numbers |
| [`(portfolio-total)`, `(portfolio-total-ret)`, `(portfolio-daily-ret)`](/posts/evaluate/#individual-metrics) | Current total value and returns |
| [`(volatility)`, `(sharpe-ratio)`, `(max-drawdown)`, `(pnl-per-trade)`](/posts/evaluate/#individual-metrics) | One metric for the run so far |
| [`(rolling-volatility)`, `(rolling-sharpe-ratio)`](/posts/evaluate/#individual-metrics) | The same over the rolling window |
| [`(update-rolling-window n)`](/posts/evaluate/#update-rolling-window) | Set the rolling window (30 trading days by default) |
| [`(benchmark-stats permno)`](/posts/evaluate/#benchmark-stats) | Returns, beta, alpha, correlation, tracking error and information ratio against a security |
| [`(plot data series x y full-date)`](/posts/evaluate/#plot) | Line chart in the browser |

## Technical indicators

| Function | What it does |
| -------- | ------------ |
| [`(moving-avg permno n)`](/posts/indicators/#moving-avg) | Simple moving average over `n` days including today |
| [`(moving-sd permno n)`](/posts/indicators/#moving-sd) | Moving standard deviation, for Bollinger Bands |
| [`(EMA permno)`](/posts/indicators/#ema), [`(EMA-generator n)`](/posts/indicators/#ema-generator) | Exponential moving average (20 days, or `n`) |
| [`(MACD permno)`](/posts/indicators/#macd), [`(MACD-generator signal short long)`](/posts/indicators/#macd-generator) | `[macd signal short-ema long-ema]` |
| [`(RSI permno)`](/posts/indicators/#rsi), [`(RSI-generator n)`](/posts/indicators/#rsi-generator) | Relative Strength Index, 0 to 100 |
| [`(ROC permno n)`](/posts/indicators/#roc) | Rate of change over `n` days |
| [`(parabolic-SAR permno state)`](/posts/indicators/#parabolic-sar) | Stop-and-reverse level; pass yesterday's result back in |
| [`(ATR permno n prev-atr)`](/posts/indicators/#atr) | Average true range |
| [`(keltner-channel permno n prev-atr)`](/posts/indicators/#keltner-channel) | `[middle upper lower]` |

Indicators return `nil` until they have enough history.

## Yahoo Finance data

| Function | What it does |
| -------- | ------------ |
| [`(download-yahoo! dir tickers & options)`](/posts/yahoo-data/#download-yahoo) | Download prices, statements and earnings dates into `<dir>/raw`; `:start`, `:fundamentals?`, `:force?` |
| [`(build-yahoo-dataset! dir & options)`](/posts/yahoo-data/#build-yahoo-dataset) | Build the dataset from the downloads, offline; `:out-dir`, `:start`, `:end`, `:report-lag-days` |
| [`(load-yahoo-dataset dir)`](/posts/yahoo-data/#load-yahoo-dataset) | Load the built prices and fundamentals |
| [`(yahoo-dataset! dir tickers & options)`](/posts/yahoo-data/#yahoo-dataset) | All three in one call |
| [`(dataset-info dir)`](/posts/yahoo-data/#dataset-info) | Coverage, options and warnings of a built dataset |

## Fundamentals

Values as they were known on the backtest date, per share of the day and in the trading currency. `nil` when not known yet.

| Function | What it does |
| -------- | ------------ |
| [`(fundamental permno field [frequency])`](/posts/fundamentals/#fundamental) | A value, such as `:net-income`; frequency `:quarterly`, `:annual`, `:trailing`, `:ttm` or `:latest` |
| [`(fundamental-info permno field [frequency])`](/posts/fundamentals/#fundamental-info) | The value with its period end, public date and source |
| [`(fundamental-history permno field frequency n)`](/posts/fundamentals/#fundamental-history) | The last `n` public values, newest first |
| [`(fundamental-as-of date permno field)`](/posts/fundamentals/#fundamental-as-of) | A value on another date |
| [`(available-fields permno)`](/posts/fundamentals/#available-fields) | The fields a security has |
| [`(pe-ratio p)`, `(earnings-yield p)`, `(book-to-market p)`, ...](/posts/fundamentals/#ratios) | Valuation ratios on today's price |
| [`(return-on-equity p)`, `(gross-margin p)`, `(debt-to-equity p)`, ...](/posts/fundamentals/#ratios) | Quality ratios |
| [`(market-cap p)`, `(enterprise-value p)`, `(shares-outstanding p)`](/posts/fundamentals/#ratios) | Size |
| [`(growth p field [frequency])`](/posts/fundamentals/#growth) | Year-on-year growth |
| [`(piotroski-f-score p)`, `(piotroski-signals p)`](/posts/fundamentals/#piotroski-f-score) | Piotroski's 0-to-9 health score and its nine signals |
| [`(latest-earnings p)`, `(days-since-earnings p)`](/posts/fundamentals/#latest-earnings) | The latest earnings announcement and its surprise |

## Automate rules

| Function | What it does |
| -------- | ------------ |
| [`(set-automation (condition f) (action g) & options)`](/posts/automation/#set-automation) | Run `g` on every trading day on which `f` returns true; returns an id |
| `:max-dispatch n`, `:expiration n` | Run at most `n` times; stay active for `n` trading days |
| [`(cancel-automation id)`](/posts/automation/#cancel-automation) | Remove an automation |
| [`(print-automation-list)`](/posts/automation/#print-automation-list) | Active automations |
| [`(print-automation-history [n])`](/posts/automation/#print-automation-history) | The days automations ran |

## Change settings

Call these before `init-portfolio`. See [Parameters](/posts/parameters/) for all of them.

| Function | Default | What it sets |
| -------- | ------- | ------------ |
| [`(update-initial-margin x)`](/posts/parameters/#update-initial-margin) | `0.5` | Margin required for new borrowing; `nil` for none |
| [`(update-maintenance-margin x)`](/posts/parameters/#update-maintenance-margin) | `0.25` | Margin below which every position is closed |
| [`(update-transaction-cost x)`](/posts/parameters/#update-transaction-cost) | `0.0` | Commission, as a fraction of the trade value |
| [`(update-interest-rate x)`](/posts/parameters/#update-interest-rate) | `0.0` | Annual interest on loans |
| [`(update-reinvest-dividends flag)`](/posts/parameters/#update-reinvest-dividends) | `true` | Reinvest dividends, or pay them into cash |
| [`(update-fundamentals-max-age months)`](/posts/parameters/#update-fundamentals-max-age) | `18` | Months after its period end a fundamental stays usable |
| [`(CHANGE-EMA-CYCLE n)`, `(CHANGE-RSI-CYCLE n)`, ...](/posts/parameters/#indicator-parameters) | `20`, `14` | Indicator periods |
| [`(alter-var-root #'PRICE-KEY (constantly :OPENPRC))`](/posts/parameters/#trading-price) | `:PRC` | Trade at the open instead of the close |
