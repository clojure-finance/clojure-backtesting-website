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

## Set up a run

| Function | What it does |
| -------- | ------------ |
| [`(load-dataset dir role [add-aprc])`](/posts/api/#load-dataset) | Load the `"main"` price dataset or a `"compustat"` fundamentals dataset |
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
| [`(CHANGE-EMA-CYCLE n)`, `(CHANGE-RSI-CYCLE n)`, ...](/posts/parameters/#indicator-parameters) | `20`, `14` | Indicator periods |
| [`(alter-var-root #'PRICE-KEY (constantly :OPENPRC))`](/posts/parameters/#trading-price) | `:PRC` | Trade at the open instead of the close |
