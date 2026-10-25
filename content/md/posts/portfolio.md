{:title "Portfolio"
 :date "2021-01-21"
 :layout :post
 :section "API Reference"
 :lead "Inspect holdings, the daily portfolio record and the filled orders."
 :toc true}

This page features functions for setting up and viewing the portfolio. The examples run on the bundled sample dataset.

## Start a Portfolio

### `init-portfolio`

This function sets the starting date and the initial capital, in cash. It is a **must** to call it before trading or using the functions that depend on the current date, e.g. `available-permnos` and `next-date`. Calling it again restarts the backtester. See [`init-portfolio`](/posts/api/#init-portfolio) for details.

**Parameters:**

- `date` - the starting date of the portfolio, in the format "YYYY-MM-DD"
- `capital` - the initial capital

**Example**:

```clojure
(init-portfolio "1990-01-02" 10000)
;; => "Date: 1990-01-02 Cash: $10000"
```

## Inspect the Portfolio

### `print-portfolio`

This function prints the current holdings in a table.

**Parameters:**

- none

**Output explanation:**

| &nbsp;**Column**&emsp; | &nbsp;**Format** &emsp; | &nbsp;**Meaning**                            |
| ------------------ | :-----------------: | :--------------------------------------- |
| `asset`            |    &nbsp;string     | Cash or the security        |
| `price`            |   &nbsp;float, $    | Price of the security at the last revaluation |
| `aprc`             |   &nbsp;float, $    | Adjusted price of the security |
| `quantity`         |   &nbsp;float       | Number of shares held; negative for a short position. Grows on dividend days when dividends are reinvested, and on splits. |
| `tot-val`          |   &nbsp;float, $    | Value of the holding (negative for a short position) or the cash balance (negative when there is a loan) |

**Example**:

```clojure
(print-portfolio)
```

```text
| :asset | :price |    :aprc | :quantity | :tot-val |
|--------+--------+----------+-----------+----------|
|   cash |    N/A |      N/A |       N/A |  5930.80 |
|  10002 | 133.11 | 133.1100 |      30.0 |  3993.30 |
```

### `print-portfolio-record`

This function prints the daily history of the portfolio's value in a table.

**Parameters:**

- [`n`] - number of rows to print, from the start. Without it, or with zero or a negative number, the entire record is printed.

**Output explanation:**

| &nbsp;**Column**&emsp; | &nbsp;**Format** &emsp; | &nbsp;**Meaning**                                                    |
| ------------------ | :-----------------: | :--------------------------------------------------------------- |
| `date`             |  &nbsp;YYYY-MM-DD   | Date of record                                             |
| `tot-value`        |    &nbsp;float, $   | Total value (equity) of the portfolio: cash plus long positions minus short positions |
| `daily-ret`        |   &nbsp;float, %    | Daily return of the portfolio (log return) |
| `tot-ret`          |   &nbsp;float, %    | Total return of the portfolio since the start (sum of log returns) |
| `loan`             |    &nbsp;float, $   | Cash borrowed, i.e. the negative part of the cash balance |
| `short`            |    &nbsp;float, $   | Market value of the borrowed (shorted) shares |
| `leverage`         |   &nbsp;float       | (loan + short) / total value |
| `margin`           |   &nbsp;float, %    | Portfolio margin: total value / gross value of all positions (long + short); 100% when there are no positions. A margin call closes all positions when it falls below `MAINTENANCE-MARGIN`. |

**Example**:

```clojure
(print-portfolio-record 3)
```

```text
|      :date | :tot-value | :daily-ret | :tot-ret | :loan | :short | :leverage | :margin |
|------------+------------+------------+----------+-------+--------+-----------+---------|
| 1990-01-05 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-08 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
| 1990-01-09 |  $10000.00 |      0.00% |    0.00% | $0.00 |  $0.00 |      0.00 | 100.00% |
```

The [Buying on Margin](/posts/examples/example-buying-on-margin/) example shows the record with loans and short positions.

### `print-order-record`

This function prints the filled orders in a table.

**Parameters:**

- [`n`] - number of rows to print, as for `print-portfolio-record`

**Example**:

```clojure
(print-order-record)
```

```text
|      :date | :permno | :price |  :aprc | :quantity |
|------------+---------+--------+--------+-----------|
| 1990-02-21 |   10001 |  48.82 |  49.32 |      20.0 |
| 1990-02-21 |   10002 | 135.08 | 135.08 |      30.0 |
| 1990-02-22 |   10001 |  47.98 |  48.47 |     -20.0 |
```

### `portfolio-exposure`

This function summarises the current portfolio in dollars.

**Parameters:**

- none

**Example**:

```clojure
(portfolio-exposure)
;; => {:cash 5930.799999999999, :long 3993.3, :short 0.0, :equity 9924.099999999999, :gross 3993.3, :loan 0.0}
```

### Raw records

The data behind these tables are atoms you can read directly, for example to plot them:

- `portfolio` - the current holdings, a map from security to `{:price :aprc :quantity :tot-val ...}` plus `:cash`
- `portfolio-value` - the portfolio record, one map per day
- `order-record` - the filled orders
- `pending-order` - orders that have not filled yet

```clojure
(deref portfolio)
;; => {:cash {:tot-val 5930.799999999999}, "10002" {:price 133.11, :aprc 133.11000000000004, :cfacpr 2.0, :quantity 30.0, :tot-val 3993.3}}
```
