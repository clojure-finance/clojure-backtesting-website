{:title "Get Started"
:date "2021-01-27"
:layout :post
:navbar? true
:page-index 1
:toc true}

---

## Requirements

- Java 11 or later
- [Leiningen](https://leiningen.org/)

## Setting Up

Clone the [repository](https://github.com/clojure-finance/clojure-backtesting) and `cd` into it:

```text
git clone https://github.com/clojure-finance/clojure-backtesting.git
cd clojure-backtesting
```

The repository ships with a small sample dataset in `resources/sample-data`, so every example below runs without downloading anything else. It holds three securities over January to March 1990:

| Security | Ticker | Notes |
| -------- | ------ | ----- |
| `10001`  | AAA    | Pays a dividend on 1990-02-15 |
| `10002`  | BBB    | Splits 2-for-1 on 1990-03-01 |
| `10003`  | CCC    | Has no prices from 1990-02-05 to 1990-02-09 |

It also has a matching Compustat-style dataset in `resources/sample-data/compustat`. See the [User Guide](/posts/user-guide) for using the full CRSP and Compustat data or your own data.

### Leiningen REPL

Start a REPL in the project directory:

```text
lein repl
```

The REPL starts in the `clojure-backtesting.user` namespace, which already refers every public function of the library. For a quick demo run without a REPL, use `lein run`.

### Gorilla REPL

[Gorilla REPL](http://gorilla-repl.org/) is a notebook in the browser, similar to Jupyter Notebook. The example strategies in the [`examples`](https://github.com/clojure-finance/clojure-backtesting/tree/master/examples) folder are Gorilla worksheets.

1. Start it in the project directory:

```text
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

![image](/img/gorilla.png)

4. Click the menu button at the top right and choose "Load a worksheet", then pick one of the worksheets in the `examples` folder.

![image](/img/load_worksheet.png)

5. Run a cell with Ctrl + Enter.

For more about Gorilla REPL, see its [website](http://gorilla-repl.org/index.html).

---

## Clojure Beginner Tutorials

If you are new to Clojure, we recommend a quick read of these tutorials first:

- [Clojure by Example](http://kimh.github.io/clojure-by-example/#about) - pick up the syntax quickly

- [ClojureDocs](https://clojuredocs.org/) - documentation and examples for the core functions

- [Clojure for the Brave and True](https://www.braveclojure.com/clojure-for-the-brave-and-true/) - a book that teaches Clojure in depth

---

## Help & Support

If you have difficulties using the backtester, find a bug, or have ideas for more examples, please open an [issue](https://github.com/clojure-finance/clojure-backtesting/issues).

---

## Code Walkthrough

This section goes through the [`simple_trading_strategy.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/simple_trading_strategy.clj) worksheet, which runs on the sample dataset.

### Import libraries

In a namespace of your own, require the library's namespaces. (In `lein repl` you can skip this: the starting namespace already has them.)

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
            [clojure-backtesting.direct :refer :all]))
```

### Import dataset

Load the dataset by giving its directory. `add-aprc` adds the adjusted price columns the backtester needs; the sample dataset already has them.

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)
```

```text
The dataset is already furnished by add-aprc. No more modification is needed.
"Date range: 1990-01-02 ~ 1990-03-30"
```

### Initialise portfolio

Pass the starting date ("YYYY-MM-DD") and the initial capital to `init-portfolio`.

```clojure
(init-portfolio "1990-01-02" 10000)
;; => "Date: 1990-01-02 Cash: $10000"
```

### Check available securities

You can check which securities trade on the current date:

```clojure
(available-permnos)
;; => ("10001" "10002" "10003")
```

### Write your strategy

The strategy below follows a simple logic. Over 10 trading days:

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

The loop calls `next-date` every time so that the backtester moves on to the next trading day (see [moving the time pointer](/posts/api#move-time-pointer)). An order fills at the close of the next trading day, which is why each `Order:` line carries the following day's date:

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

### Check order record

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

### View portfolio and portfolio record

All 50 shares have been sold, so only cash is left:

```clojure
(print-portfolio)
```

```text
| :asset | :price | :aprc | :quantity | :tot-val |
|--------+--------+-------+-----------+----------|
|   cash |    N/A |   N/A |       N/A |  9961.80 |
```

The portfolio record has one row per trading day. Pass a number to print only that many rows, or a negative number to print all of them. The columns are explained on the [Portfolio](/posts/portfolio) page.

```clojure
(print-portfolio-record -1)
```

```text
|      :date | :tot-value | :daily-ret | :tot-ret | :loan | :short | :leverage |  :margin |
|------------+------------+------------+----------+-------+--------+-----------+----------|
| 1990-01-02 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 |  100.00% |
| 1990-01-03 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 |  393.62% |
| 1990-01-04 |   $9970.50 |      0.00% |   -0.30% | $0.00 |  $0.00 |      0.00 |  496.34% |
| 1990-01-05 |   $9976.50 |      0.06% |   -0.24% | $0.00 |  $0.00 |      0.00 |  495.16% |
| 1990-01-08 |   $9977.70 |      0.00% |   -0.22% | $0.00 |  $0.00 |      0.00 |  659.90% |
| 1990-01-09 |   $9963.00 |     -0.15% |   -0.37% | $0.00 |  $0.00 |      0.00 |  665.40% |
| 1990-01-10 |   $9944.10 |      0.00% |   -0.56% | $0.00 |  $0.00 |      0.00 | 1008.94% |
| 1990-01-11 |   $9951.70 |      0.08% |   -0.48% | $0.00 |  $0.00 |      0.00 | 1001.98% |
| 1990-01-12 |   $9944.30 |      0.00% |   -0.56% | $0.00 |  $0.00 |      0.00 | 2017.51% |
| 1990-01-15 |   $9960.40 |      0.16% |   -0.40% | $0.00 |  $0.00 |      0.00 | 1956.86% |
| 1990-01-16 |   $9961.80 |      0.00% |   -0.38% | $0.00 |  $0.00 |      0.00 |  100.00% |
```

### Generate evaluation report

Because `update-eval-report` was called every day in the loop, the evaluation report has a row for each day since the first order. The metrics are explained on the [Evaluation](/posts/evaluate) page.

If you step through many dates, updating the report every day takes a lot of memory and time; update it less often, or print only a few rows.

```clojure
(print-eval-report)
```

```text
|      :date | :tot-value |    :vol |  :r-vol | :sharpe | :r-sharpe | :pnl-pt | :max-drawdown |
|------------+------------+---------+---------+---------+-----------+---------+---------------|
| 1990-01-03 |     $10000 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |      $0 |        0.0000 |
| 1990-01-04 |      $9970 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |    $-14 |        0.2950 |
| 1990-01-05 |      $9976 | 0.0301% | 0.0301% |  7.9373 |    7.9373 |    $-11 |        0.2950 |
| 1990-01-08 |      $9977 | 0.0269% | 0.0269% |  7.0993 |    7.0993 |     $-7 |        0.2950 |
| 1990-01-09 |      $9963 | 0.0694% | 0.0694% | -3.3269 |   -3.3269 |    $-12 |        0.3700 |
| 1990-01-10 |      $9944 | 0.0636% | 0.0636% | -3.1121 |   -3.1121 |    $-13 |        0.5590 |
| 1990-01-11 |      $9951 | 0.0667% | 0.0667% | -0.3235 |   -0.3235 |    $-12 |        0.5590 |
| 1990-01-12 |      $9944 | 0.0624% | 0.0624% | -0.3074 |   -0.3074 |    $-11 |        0.5590 |
| 1990-01-15 |      $9960 | 0.0782% | 0.0782% |  3.0617 |    3.0617 |     $-7 |        0.5590 |
```

### Save the records as CSV files

The records stay in memory by default. To also write them as CSV files, set an output directory before `init-portfolio`:

```clojure
(update-output-dir "out")
(init-portfolio "1990-01-02" 10000)
```

The run then writes `out_order_record.csv`, `out_portfolio_value_record.csv` and `out_evaluation_report.csv` into `out/`. The files are complete once the run ends, through `end-order` or by reaching the end of the dataset.

### Plot variables

`plot` draws a line chart in the browser. Tag each row with a series name, then pass the series key, the x key and the y key:

```clojure
;; portfolio daily return
(def data-to-plot (map #(assoc % :plot "portfolio") (deref portfolio-value)))
(plot data-to-plot :plot :date :daily-ret true)

;; volatility from the evaluation report
(def data-to-plot (map #(assoc % :plot "volatility") (deref eval-record)))
(plot data-to-plot :plot :date :vol true)
```

See [`plot`](/posts/evaluate#plot-graphs) for details, and the [`Plotting_frame.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Plotting_frame.clj) worksheet for more examples.
