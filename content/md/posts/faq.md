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

### A rebalance prints "fails due to initial margin requirement"

When a `rebalance` invests all of the portfolio, the last purchase can need more cash than is left after the sales, because prices moved overnight. Borrowing the rest must meet the initial margin, so a purchase that would mostly be paid with borrowed money fails:

```text
Order request 2023-07-04 | GAMA | 22.193675538430284 fails due to initial margin requirement.
```

Keep a small cash buffer by scaling the weights below 100%; see [Keep a cash buffer](/posts/recipes/#keep-a-cash-buffer).

### The share count has decimals

Dividends are reinvested by default: on a dividend day the holding grows by the dividend's worth of shares, such as 10 shares becoming 10.1025. Call `(update-reinvest-dividends false)` before `init-portfolio` to receive dividends as cash instead. `order-target-percent` and `rebalance` also order fractional shares, unless you pass `:whole-shares? true`.

## Indicators and data

### An indicator returns nil

Indicators need enough history. `(moving-avg "10001" 20)` returns `nil` until the security has a price on each of the last 20 trading days, so on the sample data it starts on the 20th trading day, and pauses after a gap in the prices. Guard your strategy with `when-let` or `(and price ma ...)`.

`MACD` is different: it only starts tracking a security after its first call, so call it once early in the run even though it returns `nil` then. See [MACD](/posts/indicators/#macd).

### A price or a moving average suddenly halved

That is a stock split. On BBB's 2-for-1 split on 1990-03-01 the raw closing price (`:PRC`) halves, while the adjusted price (`:APRC`) does not. The portfolio handles splits correctly, but indicators work on the raw price, so they see a crash. The [Golden Cross](/posts/examples/example-golden-cross/) example shows the effect.

### The fundamental columns are missing for a security

A Compustat filing is joined to a day only once it was public: its report date (`rdq`) and period end must be on or before that day, and the period end at most 6 months old. Securities without filings, such as CCC in the sample, have no fundamental columns at all. Values arrive as strings; convert them with `->double`. See [Fundamental Analysis](/posts/examples/example-fundamental-analysis/).

## Yahoo Finance data and fundamentals

### A fundamental or a ratio returns nil

`nil` means the value cannot be stated honestly on the backtest date. `fundamental-info` and `available-fields` usually tell you why:

- **Nothing public yet.** Yahoo shows about four fiscal years of statements, so a download's fundamentals start roughly three and a half years before it was made. Earlier dates have prices but no fundamentals.
- **Too old.** The latest value's period ended more than 18 months before the day; see [`FUNDAMENTALS-MAX-AGE-MONTHS`](/posts/parameters/#fundamentals-max-age-months).
- **No statements.** Indices, ETFs and currencies have none, and neither does a ticker downloaded with `:fundamentals? false`.
- **Not downloaded.** The field is not in [`default-fields`](/posts/yahoo-data/#default-fields); download again with `:fields` (and `:force? true` on the same day).
- **No exchange rate** for a company that reports in another currency. The build warns about it.
- **Meaningless.** `pe-ratio` is `nil` on losses, `return-on-equity` on negative equity. Use `earnings-yield`, which is negative on losses, to rank loss-makers too.
- **Share count refused.** For some companies with several share classes the share count does not match the quoted shares; the library prints a warning and leaves out every ratio on market value.

### Why does the price differ from Yahoo's website?

Yahoo's charts show old prices divided by every later split. The dataset has the prices as they were traded, so before a 4-for-1 split they are four times Yahoo's. The adjusted price `:APRC` follows the total return instead. See [Prices as traded](/posts/yahoo-data/#prices-as-traded).

### The download failed or stopped halfway

Yahoo limits how fast you may download. If the download was interrupted, run the same `download-yahoo!` call again: tickers already downloaded today are skipped, so it carries on with the rest. Tickers that failed count as downloaded too, so retry them with `:force? true`; the result lists them under `:failed`. If they keep failing, lower `:concurrency` to 1 or raise `:pause-ms`. See [Resuming and updating](/posts/yahoo-data/#resuming-and-updating).

### The build says the securities trade in different currencies

The backtester adds up all positions as one currency, so it refuses a dataset that mixes, say, dollars and euros. Download and build one dataset per currency, or pass `:allow-mixed-currencies? true` if your strategy never holds more than one of them at a time. See [Currencies](/posts/yahoo-data/#currencies).

### The build warns that report dates are estimated

Yahoo gave no report date for some periods of that ticker, so the build assumed one: 45 days after a quarter, 90 after a fiscal year. `fundamental-info` marks those values with `:report-date-source :lag`. If a result depends on them, rebuild with a different `:report-lag-days` and compare; see [Report dates](/posts/yahoo-data/#report-dates).

### How do I get more years of fundamentals?

Download again every quarter. Each download adds a snapshot, and the build merges them, so the history grows beyond the four years Yahoo shows at a time.

### My strategy looks great on Yahoo data. Is it?

Probably less great than it looks. Yahoo only has companies that still trade, so the ones that went bankrupt or were taken over are missing from your universe (*survivorship bias*). Check also how many variants you tried before this one, and whether costs are included. See [Why backtests flatter](/posts/concepts/#why-backtests-flatter).

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

Yes. Convert it to the backtester's layout with [`clojask-script`](https://github.com/clojure-finance/clojure-backtesting/tree/master/clojask-script) from the repository, then load it by directory. The columns the backtester needs are listed in [Essential column names](/posts/user-guide/#essential-column-names). Or download any ticker from [Yahoo Finance](/posts/yahoo-data/).

### The sample data is not found in my own project

Loading the sample data in a project that uses the library from Clojars fails:

```text
java.io.FileNotFoundException: resources/sample-data/main/header (No such file or directory)
```

The paths on this site are relative to a clone of the library's repository. The jar contains the sample data too, but `load-dataset` reads directories on disk, not from the classpath. Run the examples in a clone (see [Install](/posts/get-started/#install)), and point your own project at your own data.

### How do I save the results?

Set an output directory before `init-portfolio`, and the order record, portfolio record and evaluation report are written as CSV files while the run goes:

```clojure
(update-output-dir "out")
(init-portfolio "1990-01-02" 10000)
```

The raw records are also atoms you can process in Clojure: `@order-record`, `@portfolio-value` and `@eval-record`.

### Where do I find CRSP and Compustat data?

They are licensed databases, available through providers such as [WRDS](https://wrds-www.wharton.upenn.edu/), usually via a university. They are not distributed with the library; the bundled sample dataset follows their layout.
