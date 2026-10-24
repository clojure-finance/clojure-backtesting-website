{:title "Example - Fundamental Analysis"
:date "2020-12-30"
:layout :post
:tags  []
:toc true}

This walkthrough follows the worksheet [`Fundamental analysis.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Fundamental%20analysis.clj). It runs on the bundled sample dataset and can be opened in Gorilla REPL with `make start` (see [Get Started](/posts/get-started) for setup).

It implements a simple fundamental analysis strategy: rank securities by return on equity (ROE) from their quarterly filings and buy the best one.

A supplementary dataset (Compustat-style quarterly fundamentals) is joined onto each day's rows. For every security the latest filing that was public on that day is used: the filing's period-end date (`datadate`) must be on or before the day, and so must its report date (`rdq`) when the dataset has one. Without a report date the period end alone decides, which lets the strategy see the numbers before they were announced. See [API](/posts/api) for loading datasets.

---

## Initialisation

```clojure
(ns clojure-backtesting.examples.fundamental-analysis
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

Load the daily price data and the fundamentals, and start a portfolio with $10000:

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)
(load-dataset "resources/sample-data/compustat" "compustat")
(init-portfolio "1990-01-02" 10000)

;; output:
;; The dataset is already furnished by add-aprc. No more modification is needed.
;; => "Date: 1990-01-02 Cash: $10000"
```

## Which Filing Is Visible When

The sample has filings for the quarters ending 1989-09-30 and 1989-12-31. AAA (`10001`) reports the fourth quarter on 1990-01-24, so until then the September numbers are the latest public ones. `fundamentals` picks a few columns from a security's row for today: total assets (`atq`), common equity (`ceqq`) and net income (`niq`).

```clojure
(defn fundamentals [permno]
  (select-keys (get-permno-info permno) [:date :datadate :rdq :atq :ceqq :niq]))

(fundamentals "10001")

;; output:
;; => {:date "1990-01-02", :datadate "1989-09-30", :rdq "1989-10-25", :atq "1500.0", :ceqq "900.0", :niq "40.0"}
```

Step forward to 1990-01-24, the report date, and the December filing appears:

```clojure
(while (< (compare (get-date) "1990-01-24") 0)
  (next-date))
(fundamentals "10001")

;; output:
;; => {:date "1990-01-24", :datadate "1989-12-31", :rdq "1990-01-24", :atq "1550.0", :ceqq "925.0", :niq "45.0"}
```

CCC (`10003`) has no filings in the sample, so its row carries no fundamental columns:

```clojure
(fundamentals "10003")

;; output:
;; => {:date "1990-01-24"}
```

## A Return-on-Equity Screen

On 1990-02-01, once both securities with fundamentals have reported, rank them by return on equity (quarterly net income over common equity) and buy the better one. Fundamental columns arrive as strings; `->double` converts them. Securities without the data (here CCC) are dropped from the ranking.

```clojure
(while (< (compare (get-date) "1990-02-01") 0)
  (next-date))

(defn roe [permno]
  (let [row (get-permno-info permno)
        niq (->double (:niq row))
        ceqq (->double (:ceqq row))]
    (when (and niq ceqq (pos? ceqq))
      (/ niq ceqq))))

(def ranked (->> ["10001" "10002" "10003"]
                 (keep (fn [permno] (when-let [r (roe permno)] [permno r])))
                 (sort-by second >)))
ranked

;; output:
;; => (["10002" 0.07441860465116279] ["10001" 0.04864864864864865])
```

BBB (`10002`) has the higher ROE. Buy 50 shares of it, then step through the rest of the sample, updating the evaluation report each day. The order fills at the next trading day's close, and `end-order` closes the position at the end.

```clojure
(let [[best] (first ranked)]
  (order best 50 :print true))

(while (< (compare (get-date) "1990-03-29") 0)
  (update-eval-report)
  (next-date))
(end-order)

;; output:
;; Order: 1990-02-02 | 10002 | 50.000000.
;; => true
```

## Check Records and Performance

The first 10 rows of the portfolio value record cover January, before the trade:

```clojure
(print-portfolio-record 10)
```

```
|      :date | :tot-value | :daily-ret | :tot-ret | :loan | :short | :leverage | :margin |
|------------+------------+------------+----------+-------+--------+-----------+---------|
| 1990-01-02 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-03 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-04 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-05 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-08 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-09 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-10 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-11 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-12 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-15 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
```

The evaluation report starts on the day the order fills. The metrics are described in [Evaluate](/posts/evaluate).

```clojure
(print-eval-report 5)
```

```
|      :date | :tot-value |    :vol |  :r-vol | :sharpe | :r-sharpe | :pnl-pt | :max-drawdown |
|------------+------------+---------+---------+---------+-----------+---------+---------------|
| 1990-02-02 |     $10000 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |      $0 |       0.0000% |
| 1990-02-05 |     $10073 | 0.1455% | 0.1455% |  3.1749 |    3.1749 |     $73 |       0.0000% |
| 1990-02-06 |     $10140 | 0.1891% | 0.1891% |  4.4884 |    4.4884 |    $140 |       0.0000% |
| 1990-02-07 |     $10040 | 0.2728% | 0.2728% |  0.8711 |    0.8711 |     $40 |       0.9813% |
| 1990-02-08 |     $10009 | 0.2746% | 0.2746% |  0.1960 |    0.1960 |      $9 |       1.2870% |
```

## Plotting

Tag each portfolio value record with a series name and plot the total portfolio value over time. The chart opens in the browser when this cell runs in the Gorilla REPL.

```clojure
(def data-to-plot (map #(assoc % :plot "portfolio") (deref portfolio-value)))
(plot data-to-plot :plot :date :tot-value true)
```
