{:title "Get Started"
 :date "2021-01-27"
 :layout :post
 :section "Learn"
 :lead "Install the library, run your first backtest on the bundled sample data, and read the results. It takes about ten minutes."
 :toc true}

## Requirements

- Java 11 or later
- [Leiningen](https://leiningen.org/), the Clojure build tool

> **Tip:** New to Clojure? You can follow this page without knowing much of it. When you want to learn more, [Clojure by Example](http://kimh.github.io/clojure-by-example/#about) teaches the syntax quickly, [ClojureDocs](https://clojuredocs.org/) documents the core functions with examples, and [Clojure for the Brave and True](https://www.braveclojure.com/clojure-for-the-brave-and-true/) is a free book that goes deeper.

## Install

There are two ways to get the library. To follow this page and run the examples, **clone the repository**: it has the sample data and the example worksheets. To use the library in a project of your own, **add it as a dependency**.

### Clone the repository

Clone the [repository](https://github.com/clojure-finance/clojure-backtesting) and `cd` into it:

```bash
git clone https://github.com/clojure-finance/clojure-backtesting.git
cd clojure-backtesting
```

### Add it to your project

The library is on [Clojars](https://clojars.org/com.github.clojure-finance/clojure-backtesting). With Leiningen, add it to the `:dependencies` in `project.clj`:

```clojure
[com.github.clojure-finance/clojure-backtesting "1.1.3"]
```

With the Clojure CLI, add it to the `:deps` in `deps.edn`:

```clojure
com.github.clojure-finance/clojure-backtesting {:mvn/version "1.1.3"}
```

Then require the namespaces you need, as shown [below](#use-the-library-in-your-own-namespace). The paths on this site, such as `"resources/sample-data/main"`, are relative to a clone of the repository; in your own project, point `load-dataset` at your data, or [download data from Yahoo Finance](/posts/yahoo-data/).

### The sample data

The repository ships with a small sample dataset in `resources/sample-data`, so everything on this site runs without downloading anything else. It holds three securities over January to March 1990, each with a quirk that exercises part of the library:

| Security | Ticker | Notes |
| -------- | ------ | ----- |
| `10001`  | AAA    | Pays a dividend on 1990-02-15 |
| `10002`  | BBB    | Splits 2-for-1 on 1990-03-01 |
| `10003`  | CCC    | Has no prices from 1990-02-05 to 1990-02-09 |

It also has a matching Compustat-style dataset of company fundamentals in `resources/sample-data/compustat`. A second sample, in `resources/sample-yahoo`, holds synthetic Yahoo Finance downloads for 2022 to 2024, with financial statements and earnings dates; the [Yahoo Finance Data](/posts/yahoo-data/#try-it-offline) page describes it. To use the full CRSP and Compustat data or your own data, see [Data and Trading Rules](/posts/user-guide/).

## Choose a REPL

You run strategies interactively, in a REPL. There are two options.

### Option 1: Leiningen REPL

Start a REPL in the project directory:

```bash
lein repl
```

The REPL starts in the `clojure-backtesting.user` namespace, where every function of the library is already available, so you can type the code below directly. For a quick demo run without a REPL, use `lein run`.

### Option 2: Gorilla REPL

[Gorilla REPL](http://gorilla-repl.org/) is a notebook in the browser, similar to Jupyter Notebook. The example strategies in the [`examples`](https://github.com/clojure-finance/clojure-backtesting/tree/master/examples) folder are Gorilla worksheets.

1. Start it in the project directory:

   ```bash
   make start
   ```

2. When it is ready, it prints something like:

   ```text
   Gorilla-REPL: 0.6.0
   Unable to reach update server.
   Started nREPL server on port 50649
   Running at http://127.0.0.1:50651/worksheet.html .
   Ctrl+C to exit.
   ```

3. Open the URL from the output in your browser.

   ![The Gorilla REPL start page in a browser](/img/gorilla.png)

4. Click the menu button at the top right and choose "Load a worksheet", then pick one of the worksheets in the `examples` folder.

   ![The Load a worksheet dialog listing the example worksheets](/img/load_worksheet.png)

5. Run a cell with Ctrl + Enter.

For more about Gorilla REPL, see its [website](http://gorilla-repl.org/index.html).

## Your First Backtest

This section goes through the [`simple_trading_strategy.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/simple_trading_strategy.clj) worksheet step by step. Type or paste each block into the REPL.

### Step 1: Load the dataset

Load the dataset by giving its directory. `add-aprc` adds the adjusted price columns the backtester needs; the sample dataset already has them, so it only prints a note.

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)
```

```text
The dataset is already furnished by add-aprc. No more modification is needed.
"Date range: 1990-01-02 ~ 1990-03-30"
```

### Step 2: Start the portfolio

Pass the starting date ("YYYY-MM-DD") and the initial capital to `init-portfolio`. This sets the backtester's clock to that date.

```clojure
(init-portfolio "1990-01-02" 10000)
;; => "Date: 1990-01-02 Cash: $10000"
```

You can check which securities trade on the current date:

```clojure
(available-permnos)
;; => ("10001" "10002" "10003")
```

### Step 3: Write the strategy

This strategy is deliberately simple, to show the mechanics. Over 10 trading days it will:

- buy 50 shares of AAA (`10001`) on the first day
- sell 10 shares of AAA on every other day

```clojure
(def num-of-days (atom 10))

(while (pos? @num-of-days)
  (when (= 10 @num-of-days)
    (order "10001" 50 :print true)) ; buy 50 shares
  (when (odd? @num-of-days)
    (order "10001" -10 :print true)) ; sell 10 shares
  (update-eval-report) ; update the evaluation metrics every day
  (println (get-date))
  (next-date) ; move on to the next trading day
  (swap! num-of-days dec))
```

The loop calls `next-date` every time so that the backtester moves on to the next trading day. An order fills at the close of the next trading day, which is why each `Order:` line carries the following day's date:

```text
1990-01-02
Order: 1990-01-03 | 10001 | 50.000000.
1990-01-03
Order: 1990-01-04 | 10001 | -10.000000.
1990-01-04
1990-01-05
Order: 1990-01-08 | 10001 | -10.000000.
1990-01-08
1990-01-09
Order: 1990-01-10 | 10001 | -10.000000.
1990-01-10
1990-01-11
Order: 1990-01-12 | 10001 | -10.000000.
1990-01-12
1990-01-15
Order: 1990-01-16 | 10001 | -10.000000.
```

> **Why the next day?** You decide after seeing today's closing price, and nobody can trade at a price that is already in the past. Filling at the next close keeps the backtest honest. [Key Concepts](/posts/concepts/#when-an-order-fills) has a diagram.

### Step 4: Check the orders

```clojure
(print-order-record)
```

```text
|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-03 |   10001 |  50.81 | 50.81 |      50.0 |
| 1990-01-04 |   10001 |  50.22 | 50.22 |     -10.0 |
| 1990-01-08 |   10001 |   50.4 | 50.40 |     -10.0 |
| 1990-01-10 |   10001 |  49.28 | 49.28 |     -10.0 |
| 1990-01-12 |   10001 |  49.29 | 49.29 |     -10.0 |
| 1990-01-16 |   10001 |  51.04 | 51.04 |     -10.0 |
```

### Step 5: View the portfolio

All 50 shares have been sold, so only cash is left:

```clojure
(print-portfolio)
```

```text
| :asset | :price | :aprc | :quantity | :tot-val |
|--------+--------+-------+-----------+----------|
|   cash |    N/A |   N/A |       N/A |  9961.80 |
```

The portfolio record has one row per trading day. Pass a number to print only that many rows, or a negative number to print all of them. The columns are explained on the [Portfolio](/posts/portfolio/#print-portfolio-record) page.

```clojure
(print-portfolio-record -1)
```

```text
|      :date | :tot-value | :daily-ret | :tot-ret | :loan | :short | :leverage |  :margin |
|------------+------------+------------+----------+-------+--------+-----------+----------|
| 1990-01-02 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 |  100.00% |
| 1990-01-03 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 |  393.62% |
| 1990-01-04 |   $9970.50 |     -0.30% |   -0.30% | $0.00 |  $0.00 |      0.00 |  496.34% |
| 1990-01-05 |   $9976.50 |      0.06% |   -0.24% | $0.00 |  $0.00 |      0.00 |  495.16% |
| 1990-01-08 |   $9977.70 |      0.01% |   -0.22% | $0.00 |  $0.00 |      0.00 |  659.90% |
| 1990-01-09 |   $9963.00 |     -0.15% |   -0.37% | $0.00 |  $0.00 |      0.00 |  665.40% |
| 1990-01-10 |   $9944.10 |     -0.19% |   -0.56% | $0.00 |  $0.00 |      0.00 | 1008.94% |
| 1990-01-11 |   $9951.70 |      0.08% |   -0.48% | $0.00 |  $0.00 |      0.00 | 1001.98% |
| 1990-01-12 |   $9944.30 |     -0.07% |   -0.56% | $0.00 |  $0.00 |      0.00 | 2017.51% |
| 1990-01-15 |   $9960.40 |      0.16% |   -0.40% | $0.00 |  $0.00 |      0.00 | 1956.86% |
| 1990-01-16 |   $9961.80 |      0.01% |   -0.38% | $0.00 |  $0.00 |      0.00 |  100.00% |
```

The margin is above 100% because part of the portfolio is cash, so its equity is larger than its stock position. It only matters once you borrow (see [Long, short and borrowed money](/posts/concepts/#long-short-and-borrowed-money)).

### Step 6: Read the evaluation report

Because `update-eval-report` was called every day in the loop, the evaluation report has a row for each day since the first order filled. The metrics are explained on the [Evaluation](/posts/evaluate/#print-eval-report) page.

```clojure
(print-eval-report)
```

```text
|      :date | :tot-value |    :vol |  :r-vol | :sharpe | :r-sharpe | :pnl-pt | :max-drawdown |
|------------+------------+---------+---------+---------+-----------+---------+---------------|
| 1990-01-03 |     $10000 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |      $0 |       0.0000% |
| 1990-01-04 |      $9970 | 0.1706% | 0.1706% | -9.1652 |   -9.1652 |    $-14 |       0.2950% |
| 1990-01-05 |      $9976 | 0.1603% | 0.1603% | -5.8258 |   -5.8258 |    $-11 |       0.2950% |
| 1990-01-08 |      $9977 | 0.1424% | 0.1424% | -4.9785 |   -4.9785 |     $-7 |       0.2950% |
| 1990-01-09 |      $9963 | 0.1341% | 0.1341% | -7.3148 |   -7.3148 |    $-12 |       0.3700% |
| 1990-01-10 |      $9944 | 0.1316% | 0.1316% | -9.6582 |   -9.6582 |    $-13 |       0.5590% |
| 1990-01-11 |      $9951 | 0.1338% | 0.1338% | -7.1789 |   -7.1789 |    $-12 |       0.5590% |
| 1990-01-12 |      $9944 | 0.1253% | 0.1253% | -7.8645 |   -7.8645 |    $-11 |       0.5590% |
| 1990-01-15 |      $9960 | 0.1377% | 0.1377% | -4.5745 |   -4.5745 |     $-7 |       0.5590% |
```

> **Watch out:** updating the report every day takes memory and time over long runs. Over many years, update it less often, or print only a few rows.

### Step 7: Save and plot the results

The records stay in memory by default. To also write them as CSV files, set an output directory before `init-portfolio`:

```clojure
(update-output-dir "out")
(init-portfolio "1990-01-02" 10000)
```

The run then writes `out_order_record.csv`, `out_portfolio_value_record.csv` and `out_evaluation_report.csv` into `out/`. The files are complete once the run ends, through `end-order` or by reaching the end of the dataset.

`plot` draws a line chart in the browser. Tag each row with a series name, then pass the series key, the x key and the y key:

```clojure
;; portfolio daily return
(def data-to-plot (map #(assoc % :plot "portfolio") (deref portfolio-value)))
(plot data-to-plot :plot :date :daily-ret true)

;; volatility from the evaluation report
(def data-to-plot (map #(assoc % :plot "volatility") (deref eval-record)))
(plot data-to-plot :plot :date :vol true)
```

See [`plot`](/posts/evaluate/#plot) for details, and the [`Plotting_frame.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Plotting_frame.clj) worksheet for more examples.

## Use the library in your own namespace

`lein repl` already has every function loaded. In a file or namespace of your own, require the library's namespaces:

```clojure
(ns my-strategy
  (:require [clojure-backtesting.data :refer :all]
            [clojure-backtesting.data-management :refer :all]
            [clojure-backtesting.portfolio :refer :all]
            [clojure-backtesting.order :refer :all]
            [clojure-backtesting.evaluate :refer :all]
            [clojure-backtesting.plot :refer :all]
            [clojure-backtesting.counter :refer :all]
            [clojure-backtesting.automation :refer :all]
            [clojure-backtesting.parameters :refer :all]
            [clojure-backtesting.indicators :refer :all]
            [clojure-backtesting.direct :refer :all]
            [clojure-backtesting.fundamentals :refer :all]   ; point-in-time fundamentals and ratios
            [clojure-backtesting.strategy :refer :all]       ; ranking and rebalancing
            [clojure-backtesting.yahoo :refer :all]))        ; Yahoo Finance data
```

Leave out the namespaces you don't use. `plot` needs a browser, and `automation` is only for [automations](/posts/automation/).

## Try fundamentals

The sample above has three securities and only a few Compustat-style filings. The Yahoo sample has full company reports and earnings dates, so you can try the fundamentals functions in a few lines. Build it, load it, and ask on a given day what each company was worth and earned:

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
(init-portfolio "2024-11-15" 100000)

(pe-ratio "ALFA")            ;; => 18.632838423003843
(rank-by earnings-yield)     ;; => [["ALFA" 0.05366868843586461] ["BETA" 0.03332584981650266] ["GAMA" 0.027329231371534268]]
```

The answers use only the reports that were public on 2024-11-15. With a network connection, `download-yahoo!` fetches the same kind of data for real tickers; [Yahoo Finance Data](/posts/yahoo-data/) explains how, and the [Value Strategy](/posts/examples/example-value-strategy/) example builds a complete strategy on it.

## Next steps

- [Key Concepts](/posts/concepts/) explains the ideas behind what you just ran: the clock, order fills, prices, margin and the metrics.
- The [Cheat Sheet](/posts/cheat-sheet/) lists every function on one page.
- The [examples](/posts/examples/) are complete strategies to read and modify, and the [Strategy Recipes](/posts/recipes/) are patterns to reuse.
- [Yahoo Finance Data](/posts/yahoo-data/) and [Fundamentals](/posts/fundamentals/) cover real data and company reports.
- Stuck? Try the [FAQ](/posts/faq/), or open an [issue](https://github.com/clojure-finance/clojure-backtesting/issues) if you find a bug or have an idea for an example.
