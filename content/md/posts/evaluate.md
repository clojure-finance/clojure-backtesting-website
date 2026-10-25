{:title "Evaluation and Plots"
 :date "2021-01-20"
 :layout :post
 :section "API Reference"
 :lead "Compute performance metrics, print the evaluation report and draw charts."
 :toc true}

This page features functions for computing metrics that evaluate portfolio performance, printing the evaluation report, and plotting.

## Record the Metrics

### `update-eval-report`

This function computes the evaluation metrics for today and adds them to the evaluation report (see `print-eval-report` for the list of metrics). It does nothing before the first order has filled or after the run has ended.

**Arguments:** none

**Return**

`nil`

**Example**

```clojure
=> (update-eval-report)
;; update the evaluation metrics today
```

Call it on the days you want in the report, typically once per day after `next-date`. Over many dates, updating every day takes a lot of memory and time.

## Read the Report

### `print-eval-report`

This function prints the evaluation report in a table.

**Parameters:**

- [`n`] - number of rows to print, from the start. Without it, or with zero or a negative number, the entire report is printed.

**Output explanation:**

| &nbsp;Column&emsp; | &nbsp;Format &emsp; | &nbsp;Meaning                                                                      |
| ------------------ | :-----------------: | :--------------------------------------------------------------------------------- |
| `date`             |  &nbsp;YYYY-MM-DD   | &nbsp;Date of record                                                               |
| `tot-value`        |    &nbsp;int, $     | &nbsp;Total value of the portfolio (cash plus long positions minus short positions) |
| `vol`              |   &nbsp;float, %    | &nbsp;Volatility: sample standard deviation of the daily returns since the start |
| `r-vol`            |   &nbsp;float, %    | &nbsp;Volatility over the rolling window (the last 30 days by default)           |
| `sharpe`           |   &nbsp;float       | &nbsp;Annualised Sharpe ratio since the start: mean daily return / standard deviation × √252, with a zero risk-free rate |
| `r-sharpe`         |   &nbsp;float       | &nbsp;Annualised Sharpe ratio over the rolling window                            |
| `pnl-pt`           |   &nbsp;int, $      | &nbsp;Profit and loss per trade: (total value − initial capital) / number of filled orders, not counting the orders `end-order` places to close positions |
| `max-drawdown`     |   &nbsp;float, %    | &nbsp;Maximum drawdown: the largest decline of the total value from a previous peak, as a percentage of that peak |

The daily returns are the log returns in the `daily-ret` column of the [portfolio record](/posts/portfolio/#print-portfolio-record).

**Example**:

```clojure
(print-eval-report 5)
```

```text
|      :date | :tot-value |    :vol |  :r-vol | :sharpe | :r-sharpe | :pnl-pt | :max-drawdown |
|------------+------------+---------+---------+---------+-----------+---------+---------------|
| 1990-01-03 |     $10000 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |      $0 |       0.0000% |
| 1990-01-04 |      $9970 | 0.1706% | 0.1706% | -9.1652 |   -9.1652 |    $-14 |       0.2950% |
| 1990-01-05 |      $9976 | 0.1603% | 0.1603% | -5.8258 |   -5.8258 |    $-11 |       0.2950% |
| 1990-01-08 |      $9977 | 0.1424% | 0.1424% | -4.9785 |   -4.9785 |     $-7 |       0.2950% |
| 1990-01-09 |      $9963 | 0.1341% | 0.1341% | -7.3148 |   -7.3148 |    $-12 |       0.3700% |
```

The same rows are kept as numbers (not formatted) in the `eval-record` atom, which is handy for plotting.

### Individual metrics

These functions compute a metric for the current state without touching the report. The examples were taken during a run on the sample dataset.

| Function | Return |
| -------- | ------ |
| `portfolio-total` | Current total value of the portfolio |
| `portfolio-daily-ret` | Today's daily (log) return |
| `portfolio-total-ret` | Total (log) return since the start |
| `get-daily-returns` | All daily returns so far |
| `volatility` / `rolling-volatility` | Volatility since the start / over the rolling window, as a fraction |
| `sharpe-ratio` / `rolling-sharpe-ratio` | Annualised Sharpe ratio since the start / over the rolling window |
| `pnl-per-trade` | Profit and loss per filled order of the strategy, in dollars |
| `max-drawdown` | Maximum drawdown, as a fraction of the peak |

**Example**:

```clojure
(portfolio-total)
;; => 9924.099999999999

(portfolio-total-ret)
;; => -0.007618950633236242

(pnl-per-trade)
;; => -25.300000000000484

(max-drawdown)
;; => 0.007590000000000145
```

## Rolling Window

### `update-rolling-window`

This function sets the time window, in trading days, for the rolling metrics (`r-vol` and `r-sharpe`). By default, the window is `30`.

**Parameters:**

- `n` - the new time window

**Example**:

```clojure
(update-rolling-window 10)
;; output:
Time window is updated as 10.
```

## Plotting

### `plot`

This function draws a line chart with [Oz](https://github.com/metasoarous/oz) (Vega-Lite), which opens in the browser. It works from a plain REPL as well as from Gorilla REPL.

**Parameters:**

- `dataset` - a sequence of maps, e.g. `(deref portfolio-value)`, `(deref eval-record)` or `(deref order-record)`
- `series` - the key whose value names the line each row belongs to, shown in the legend
- `x` - the key of the x-axis (time) values, e.g. `:date`
- `y` - the key of the y-axis values, e.g. `:tot-value`. Pass two keys, `y1 y2`, to draw both on independent y-axes.
- `full-date` - `true` to label the x-axis with full dates (year, month, day); `false` lets the chart choose the labels

**Example**:

```clojure
;; tag every row with the series name
(def data-to-plot
  (map #(assoc % :plot "sharpe ratio") (deref eval-record)))

(plot data-to-plot :plot :date :sharpe true)
```

To compare securities, give each row the security as its series:

```clojure
(def prices
  (for [day (reverse (get-prev-n-days 20)) row day]
    (select-keys row [:PERMNO :date :PRC])))

(plot prices :PERMNO :date :PRC true)
```

The [`Plotting_frame.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Plotting_frame.clj) worksheet has more examples.
