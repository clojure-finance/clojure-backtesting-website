{:title "FAQ and Troubleshooting"
 :date "2026-10-25"
 :layout :post
 :section "Help"
 :lead "Answers to the questions that come up most when writing a first strategy."
 :toc true}

If your question is not here, please open an [issue on GitHub](https://github.com/clojure-finance/clojure-backtesting/issues).

## Orders

### I placed an order, but the portfolio and the order record did not change

Orders fill at the close of the **next** trading day, so nothing changes until you call `next-date`. Until then the order is pending:

```clojure
(order "10001" 50)
@pending-order
;; => {["1990-01-05" "10001"] {:place "1990-01-02", :expire "1990-01-05", :permno "10001", :quantity 50, ...}}
(next-date)
(print-order-record)   ; now shows the fill
```

See [When an order fills](/posts/concepts/#when-an-order-fills).

### An order never filled

Either the security had no price for the order's whole lifetime (3 trading days by default; pass `:expiration n` for longer), or the order failed. Failures are printed when the order comes up for filling:

```text
Order request 1990-01-03 | 10001 | 10 fails.
Failure reason: You do not have enough money to buy or have enough stock to sell. Try to solve by enabling leverage.
```

This one was placed with `:leverage false` and there was not enough cash. With leverage (the default), an order fails instead when the borrowing would break the initial margin:

```text
Order request 1990-01-03 | 10001 | 10 fails due to initial margin requirement.
```

See [Buying on Margin](/posts/examples/example-buying-on-margin/) for both cases.

### I sold shares and ended up with a negative position

Selling more shares than you hold sells the rest short. This often happens when two rules sell the same position, for example a stop-loss and a take-profit automation, or a sell signal that fires on several days in a row. To sell only what you hold, sell *down to* zero:

```clojure
(order "10001" 0 :remaining true)
```

### The share count has decimals

Dividends are reinvested by default: on a dividend day the holding grows by the dividend's worth of shares, such as 10 shares becoming 10.1025. Call `(update-reinvest-dividends false)` before `init-portfolio` to receive dividends as cash instead.

## Indicators and data

### An indicator returns nil

Indicators need enough history. `(moving-avg "10001" 20)` returns `nil` until the security has a price on each of the last 20 trading days, so on the sample data it starts on the 20th trading day, and pauses after a gap in the prices. Guard your strategy with `when-let` or `(and price ma ...)`.

`MACD` is different: it only starts tracking a security after its first call, so call it once early in the run even though it returns `nil` then. See [MACD](/posts/indicators/#macd).

### A price or a moving average suddenly halved

That is a stock split. On BBB's 2-for-1 split on 1990-03-01 the raw closing price (`:PRC`) halves, while the adjusted price (`:APRC`) does not. The portfolio handles splits correctly, but indicators work on the raw price, so they see a crash. The [Golden Cross](/posts/examples/example-golden-cross/) example shows the effect.

### The fundamental columns are missing for a security

A Compustat filing is joined to a day only once it was public: its report date (`rdq`) and period end must be on or before that day, and the period end at most 6 months old. Securities without filings, such as CCC in the sample, have no fundamental columns at all. Values arrive as strings; convert them with `->double`. See [Fundamental Analysis](/posts/examples/example-fundamental-analysis/).

## The run

### next-date returns nil, and orders do nothing

The run has ended. That happens when you call `end-order`, when the clock reaches the last date of the dataset, when the portfolio value drops below zero, or on a margin call. The backtester prints the reason, for example:

```text
You have reached the end of the dataset. No more orders are allowed.
To restart. Please call init-portfolio again.
```

Call `init-portfolio` to start a new run.

### How do I restart a backtest?

Call `init-portfolio` again. It resets the portfolio, every record, the indicators and the automations, and drops any orders that have not filled yet. Settings changed with the `update-...` functions and `alter-var-root` stay as they are; set them back yourself if a later run should use the defaults.

### init-portfolio fails with an assertion error

- *"The main dataset has no APRC column"*: load the main dataset with `add-aprc` as its third argument: `(load-dataset dir "main" add-aprc)`.
- *"Please do not start from the last date"*: choose a start date before the last date of the dataset.

### The evaluation report is empty, or starts later than the portfolio record

The report only gets a row when you call `update-eval-report`, and only after the first order has filled. Call it every day in your loop, after or before `next-date`.

### The Sharpe ratio is extreme in the first rows

Early in a run the metrics are computed from a handful of daily returns, so one good or bad day dominates them. Look at the last rows, over a run long enough to mean something.

## Performance

### The backtest is slow or uses a lot of memory

- **The first load of a large dataset.** `add-aprc` writes the adjusted prices into the dataset files the first time, which can take hours on the full CRSP data. Later loads skip it.
- **Looking far back.** Data within the cache (60 days by default) is fast; older days are read from disk again. Raise the cache with `(CHANGE-CACHE-SIZE n)` if you have the memory; see [Cache Mechanism](/posts/user-guide/#cache-mechanism).
- **The evaluation report.** `update-eval-report` recomputes the metrics over the whole run. Over many years, call it once a week or month instead of every day.

## Data and output

### Can I use my own data?

Yes. Convert it to the backtester's layout with [`clojask-script`](https://github.com/clojure-finance/clojure-backtesting/tree/master/clojask-script) from the repository, then load it by directory. The columns the backtester needs are listed in [Essential column names](/posts/user-guide/#essential-column-names).

### How do I save the results?

Set an output directory before `init-portfolio`, and the order record, portfolio record and evaluation report are written as CSV files while the run goes:

```clojure
(update-output-dir "out")
(init-portfolio "1990-01-02" 10000)
```

The raw records are also atoms you can process in Clojure: `@order-record`, `@portfolio-value` and `@eval-record`.

### Where do I find CRSP and Compustat data?

They are licensed databases, available through providers such as [WRDS](https://wrds-www.wharton.upenn.edu/), usually via a university. They are not distributed with the library; the bundled sample dataset follows their layout.
