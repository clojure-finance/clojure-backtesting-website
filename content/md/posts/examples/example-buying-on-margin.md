{:title "Example: Buying on Margin"
 :date "2020-12-29"
 :layout :post
 :section "Examples"
 :lead "Borrowing cash and shares: leverage, initial and maintenance margins, and short selling."
 :toc true}

This walkthrough follows the worksheet [`buying_on_margin.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/buying_on_margin.clj). It runs on the bundled sample dataset and can be opened in Gorilla REPL with `make start` (see [Get Started](/posts/get-started/) for setup).

It shows how the backtester handles trading with and without leverage, the initial and maintenance margin requirements, and short selling. All trades use security `10001` ("AAA"), which trades at about $50 in the sample data. Orders fill at the next trading day's close.

The portfolio record reports two ratios:

- **leverage** = (loan + value of shorted stock) / equity
- **margin** = equity / gross position value (100% when there are no positions)

The margin parameters are described in [Parameters](/posts/parameters/), and the portfolio functions in [Portfolio](/posts/portfolio/).

## Initialisation

```clojure
(ns clojure-backtesting.examples.buy-on-margin
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

## Trade without Leverage

With `:leverage false` the order fills only if you have the cash to pay for it. Here $1000 is enough for 10 shares.

```clojure
(init-portfolio "1990-01-02" 1000)
(order "10001" 10 :leverage false :remaining true :print true)
(next-date)
(next-date)
(print-order-record)
(print-portfolio)
(print-portfolio-record -1)
```

```
Order: 1990-01-03 | 10001 | 10.000000.

|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-03 |   10001 |  50.81 | 50.81 |      10.0 |

| :asset | :price |   :aprc | :quantity | :tot-val |
|--------+--------+---------+-----------+----------|
|   cash |    N/A |     N/A |       N/A |   491.90 |
|  10001 |  50.22 | 50.2200 |      10.0 |   502.20 |

|      :date | :tot-value | :daily-ret | :tot-ret | :loan | :short | :leverage | :margin |
|------------+------------+------------+----------+-------+--------+-----------+---------|
| 1990-01-02 |   $1000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-03 |   $1000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 196.81% |
| 1990-01-04 |    $994.10 |     -0.59% |   -0.59% | $0.00 |  $0.00 |      0.00 | 197.95% |
```

With insufficient cash, the order is rejected when it comes up for filling.

```clojure
(init-portfolio "1990-01-02" 400)
(order "10001" 10 :leverage false :remaining true :print true)
(next-date)
(next-date)
(print-order-record)
(print-portfolio)
```

```
Order request 1990-01-03 | 10001 | 10 fails.
Failure reason: You do not have enough money to buy or have enough stock to sell. Try to solve by enabling leverage.

| :asset | :price | :aprc | :quantity | :tot-val |
|--------+--------+-------+-----------+----------|
|   cash |    N/A |   N/A |       N/A |   400.00 |
```

## Trade with Leverage

Leverage is on by default (`LEVERAGE`). The shortfall is borrowed: cash goes negative by the amount of the loan, and the portfolio record shows the loan, the leverage and the margin.

```clojure
(init-portfolio "1990-01-02" 300)
(order "10001" 10 :print true)
(next-date)
(next-date)
(print-order-record)
(print-portfolio)
(print-portfolio-record -1)
```

```
Order: 1990-01-03 | 10001 | 10.000000.

|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-03 |   10001 |  50.81 | 50.81 |      10.0 |

| :asset | :price |   :aprc | :quantity | :tot-val |
|--------+--------+---------+-----------+----------|
|   cash |    N/A |     N/A |       N/A |  -208.10 |
|  10001 |  50.22 | 50.2200 |      10.0 |   502.20 |

|      :date | :tot-value | :daily-ret | :tot-ret |   :loan | :short | :leverage | :margin |
|------------+------------+------------+----------+---------+--------+-----------+---------|
| 1990-01-02 |    $300.00 |      0.00% |    0.00% |   $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-03 |    $300.00 |      0.00% |    0.00% | $208.10 |  $0.00 |      0.69 |  59.04% |
| 1990-01-04 |    $294.10 |     -1.99% |   -1.99% | $208.10 |  $0.00 |      0.71 |  58.56% |
```

## Initial Margin

A purchase on margin is allowed only if the cash on hand covers at least `INITIAL-MARGIN` of the order's value (the loan plus the cash). The default is 0.5, so the $300 order above (about 59% covered) went through. Raising the initial margin to 0.9 rejects the same order; the cell then restores the default.

```clojure
(println INITIAL-MARGIN)
(update-initial-margin 0.9)
(init-portfolio "1990-01-02" 300)
(order "10001" 10 :print true)
(next-date)
(print-order-record)
(update-initial-margin 0.5)
```

```
0.5
Order request 1990-01-03 | 10001 | 10 fails due to initial margin requirement.
```

Set it to `nil` to disable the initial margin requirement altogether.

## Maintenance Margin

All positions are closed automatically if the portfolio margin falls below `MAINTENANCE-MARGIN` (0.25 by default). Raising it above the current margin triggers the liquidation on the next day; the cell then restores the default.

```clojure
(init-portfolio "1990-01-02" 300)
(order "10001" 10 :print true)
(next-date)
(print-portfolio-record -1)
(update-maintenance-margin 0.8)
(next-date)
(print-portfolio)
(update-maintenance-margin 0.25)
```

```
Order: 1990-01-03 | 10001 | 10.000000.

|      :date | :tot-value | :daily-ret | :tot-ret |   :loan | :short | :leverage | :margin |
|------------+------------+------------+----------+---------+--------+-----------+---------|
| 1990-01-02 |    $300.00 |      0.00% |    0.00% |   $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-03 |    $300.00 |      0.00% |    0.00% | $208.10 |  $0.00 |      0.69 |  59.04% |
1990-01-04: Portfolio margin 0.5856232576662683 (equity / gross position value) is below the maintenance margin 0.8. Closing all positions.
To restart. Please call init-portfolio again.

| :asset | :price | :aprc | :quantity | :tot-val |
|--------+--------+-------+-----------+----------|
|   cash |    N/A |   N/A |       N/A |   294.10 |
```

After a liquidation the run is over: call `init-portfolio` again to start a new one.

## Short Selling

Selling shares you do not hold borrows them. The proceeds arrive in cash, the position is negative, and the borrowed stock counts towards the gross position value in the margin. A short that would breach the initial margin is rejected: here the second order would take the short position to 60 shares (about $3000) against roughly $1000 of equity.

```clojure
(init-portfolio "1990-01-02" 1000)
(order "10001" -10 :print true)
(next-date)
(order "10001" -50 :print true)
(next-date)
(print-order-record)
(print-portfolio)
(print-portfolio-record -1)
```

```
Order: 1990-01-03 | 10001 | -10.000000.
Order request 1990-01-04 | 10001 | -50 fails due to initial margin requirement on the short position.

|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-03 |   10001 |  50.81 | 50.81 |     -10.0 |

| :asset | :price |   :aprc | :quantity | :tot-val |
|--------+--------+---------+-----------+----------|
|   cash |    N/A |     N/A |       N/A |  1508.10 |
|  10001 |  50.22 | 50.2200 |     -10.0 |  -502.20 |

|      :date | :tot-value | :daily-ret | :tot-ret | :loan |  :short | :leverage | :margin |
|------------+------------+------------+----------+-------+---------+-----------+---------|
| 1990-01-02 |   $1000.00 |      0.00% |    0.00% | $0.00 |   $0.00 |      0.00 | 100.00% |
| 1990-01-03 |   $1000.00 |     -0.00% |   -0.00% | $0.00 | $508.10 |      0.51 | 196.81% |
| 1990-01-04 |   $1005.90 |      0.59% |    0.59% | $0.00 | $502.20 |      0.50 | 200.30% |
```
