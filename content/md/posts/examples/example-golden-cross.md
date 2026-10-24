{:title "Example - Golden Cross"
 :date "2020-12-31"
 :layout :post
 :tags  []
 :toc true}

This walkthrough follows the worksheet [`golden_cross.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/golden_cross.clj). It runs on the bundled sample dataset and can be opened in Gorilla REPL with `make start` (see [Get Started](/posts/get-started) for setup).

A golden cross is when a short-term moving average of a security's price crosses above a long-term one, read as a buy signal; a death cross is the opposite, the short-term average falling below the long-term one. The classic pair is the 50-day and 200-day averages. The sample data covers only January to March 1990, so the worksheet uses 15-day and 30-day averages instead, on securities `10001` ("AAA") and `10002` ("BBB").

---

## Initialisation

```clojure
(ns clojure-backtesting.examples.golden-cross
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

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)

;; output:
;; The dataset is already furnished by add-aprc. No more modification is needed.
;; => "Date range: 1990-01-02 ~ 1990-03-30"
```

### Initialise portfolio

Go back to this cell every time you want to restart the backtest.

```clojure
(init-portfolio "1990-01-02" 10000)

;; output:
;; => "Date: 1990-01-02 Cash: $10000"
```

## Writing the Strategy

Each trading day, `golden-cross` compares the 15-day and 30-day moving averages of a security's closing price ([`moving-avg`](/posts/indicators)):

- MA 15 above MA 30 (golden cross): buy `quantity` more shares, so the position grows every day the golden cross holds
- MA 15 below MA 30 (death cross): sell the whole position (`:remaining true` with a target of 0)

`moving-avg` returns nil until 30 closes are available, so the strategy does nothing for the first 30 trading days. The loop trades AAA in lots of 10 and BBB in lots of 5, updates the evaluation report, and moves to the next day until 1990-03-29. `end-order` then closes any open positions and ends the run.

```clojure
(defn golden-cross [permno quantity]
  (let [ma15 (moving-avg permno 15)
        ma30 (moving-avg permno 30)]
    (when (and ma15 ma30)
      (if (> ma15 ma30)
        (order permno quantity :print false)
        (order permno 0 :remaining true)))))

(while (< (compare (get-date) "1990-03-29") 0)
  (golden-cross "10001" 10)
  (golden-cross "10002" 5)
  (update-eval-report)
  (next-date))
(end-order)

;; output:
;; => true
```

## Check Records

The first 10 orders: BBB's averages cross first, and the strategy adds 5 shares a day from 1990-02-13.

```clojure
(print-order-record 10)
```

```
|      :date | :permno | :price |  :aprc | :quantity |
|------------+---------+--------+--------+-----------|
| 1990-02-13 |   10002 | 128.45 | 128.45 |       5.0 |
| 1990-02-14 |   10002 | 129.49 | 129.49 |       5.0 |
| 1990-02-15 |   10002 | 129.32 | 129.32 |       5.0 |
| 1990-02-16 |   10002 |  131.6 | 131.60 |       5.0 |
| 1990-02-19 |   10002 | 132.31 | 132.31 |       5.0 |
| 1990-02-20 |   10002 |  134.2 | 134.20 |       5.0 |
| 1990-02-21 |   10002 | 135.08 | 135.08 |       5.0 |
| 1990-02-22 |   10002 | 133.11 | 133.11 |       5.0 |
| 1990-02-23 |   10002 | 133.44 | 133.44 |       5.0 |
| 1990-02-26 |   10002 | 131.03 | 131.03 |       5.0 |
```

By the end of the run every position has been sold again, so the final portfolio is all cash. `moving-avg` works on raw closing prices, so BBB's 2-for-1 split on 1990-03-01 halves its price in the averages and produces a death cross a day later.

```clojure
(print-portfolio)
```

```
| :asset | :price | :aprc | :quantity | :tot-val |
|--------+--------+-------+-----------+----------|
|   cash |    N/A |   N/A |       N/A |  9960.90 |
```

The first 10 rows of the portfolio value record, from before the first trade:

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

## Evaluation Report

`update-eval-report` adds a row only once the order record is non-empty, so the report starts on the day of the first fill. The metrics are described in [Evaluate](/posts/evaluate).

```clojure
(print-eval-report 10)
```

```
|      :date | :tot-value |    :vol |  :r-vol | :sharpe | :r-sharpe | :pnl-pt | :max-drawdown |
|------------+------------+---------+---------+---------+-----------+---------+---------------|
| 1990-02-13 |     $10000 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |      $0 |       0.0000% |
| 1990-02-14 |     $10005 | 0.0092% | 0.0095% |  2.8062 |    2.8983 |      $2 |       0.0000% |
| 1990-02-15 |     $10003 | 0.0096% | 0.0101% |  1.7520 |    1.8358 |      $1 |       0.0170% |
| 1990-02-16 |     $10037 | 0.0591% | 0.0629% |  2.9721 |    3.1653 |      $9 |       0.0170% |
| 1990-02-19 |     $10051 | 0.0623% | 0.0671% |  3.7710 |    4.0829 |     $10 |       0.0170% |
| 1990-02-20 |     $10099 | 0.0974% | 0.1062% |  4.4646 |    4.9169 |     $16 |       0.0170% |
| 1990-02-21 |     $10125 | 0.1035% | 0.1138% |  5.1732 |    5.8009 |     $17 |       0.0170% |
| 1990-02-22 |     $10056 | 0.1547% | 0.1746% |  1.5236 |    1.7108 |      $7 |       0.6810% |
| 1990-02-23 |     $10069 | 0.1538% | 0.1757% |  1.8405 |    2.0946 |      $7 |       0.6810% |
| 1990-02-26 |      $9961 | 0.2310% | 0.2678% | -0.6654 |   -0.7653 |     $-3 |       1.6216% |
```

## Plotting

Tag each portfolio value record with a series name, then plot the daily return over time with `plot` (see [Evaluate](/posts/evaluate)).

```clojure
(def data-to-plot (map #(assoc % :plot "portfolio") (deref portfolio-value)))
(first data-to-plot)

;; output:
;; => {:plot "portfolio", :date "1990-01-02", :leverage 0.0, :short 0.0, :tot-ret 0.0, :loan 0.0, :tot-value 10000, :daily-ret 0.0, :margin 1.0}
```

```clojure
(plot data-to-plot :plot :date :daily-ret true)
```

The chart opens in the browser when this cell runs in the Gorilla REPL.
