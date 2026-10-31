{:title "Parameters"
 :date "2021-01-23"
 :layout :post
 :section "API Reference"
 :lead "Settings for margins, costs, dividends, output files, fundamentals and indicator periods."
 :toc true}

The namespace `clojure-backtesting.parameters` defines the parameters that are configurable in the backtester. Most have an update function; call it before `init-portfolio`, so that the whole run uses the same settings.

The parameters without an update function can be changed for a session with `alter-var-root`, for example:

```clojure
(alter-var-root #'MISSING-DAYS-LIMIT (constantly 20))
```

## Overview

| Parameter | Default | Change with | Meaning |
| --------- | ------- | ----------- | ------- |
| `PRICE-KEY` | `:PRC` | `alter-var-root` | The column orders trade at; see [Trading Price](#trading-price) |
| `ORDER-EXPIRATION` | `3` | `alter-var-root`, or `:expiration` per order | Trading days an order stays pending |
| `LEVERAGE` | `true` | `alter-var-root`, or `:leverage` per order | Whether orders may borrow cash or shares |
| `PRINT` | `false` | `alter-var-root`, or `:print` per order | Whether orders are printed when they fill |
| `DIRECT` | `true` | `alter-var-root`, or `:direct` per order | Whether orders are written to the order CSV file |
| `INITIAL-MARGIN` | `0.5` | `update-initial-margin` | See [Margin Requirements](#margin-requirements) |
| `MAINTENANCE-MARGIN` | `0.25` | `update-maintenance-margin` | See [Margin Requirements](#margin-requirements) |
| `TRANSACTION-COST` | `0.0` | `update-transaction-cost` | Commission, as a fraction of the trade value |
| `INTEREST-RATE` | `0.0` | `update-interest-rate` | Annual interest rate on loans |
| `REINVEST-DIVIDENDS` | `true` | `update-reinvest-dividends` | See [Dividends](#dividends) |
| `OUTPUT-DIR` | `nil` | `update-output-dir` | See [Output Files](#output-files) |
| `MISSING-DAYS-LIMIT` | `10` | `alter-var-root` | Trading days without a price after which a holding counts as delisted |
| `MERGE-MAX-AGE-MONTHS` | `6` | `alter-var-root` | Months after which a Compustat filing is no longer joined |
| `FUNDAMENTALS-MAX-AGE-MONTHS` | `18` | `update-fundamentals-max-age` | See [Fundamentals](#fundamentals) |
| `CACHE-SIZE` | `60` | `CHANGE-CACHE-SIZE` | Days of data kept in memory; see [Data and Trading Rules](/posts/user-guide/#cache-mechanism) |
| `EMA-CYCLE`, `MACD-SIGNAL`, `MACD-SHORT`, `MACD-LONG`, `RSI-CYCLE` | `20`, `9`, `12`, `26`, `14` | `CHANGE-{name}` | See [Indicator Parameters](#indicator-parameters) |
| `SAR-AF-STEP`, `SAR-AF-MAX` | `0.02`, `0.2` | `alter-var-root` | Acceleration factor of `parabolic-SAR` |

## Trading Price

Orders trade at the closing price by default. To trade at the opening price instead, change `PRICE-KEY` to the opening price column:

```clojure
(alter-var-root #'PRICE-KEY (constantly :OPENPRC))
```

## Margin Requirements

Buying for more than your cash borrows the difference (a loan), and selling shares you do not hold borrows the shares (a short position). The **portfolio margin** is the total value (equity) of the portfolio divided by the gross value of all positions, long plus short.

### `INITIAL-MARGIN`

The share of an order's value that you must pay with your own cash when buying on margin; for a short sale, the portfolio margin required after the sale. An order that does not meet it fails. The default value is `0.5`; `nil` removes the requirement.

**Example:**

```clojure
(println INITIAL-MARGIN)
;; output:
0.5
```

### `update-initial-margin`

This function updates the initial margin.

**Parameters:**

- `num` - the new initial margin, greater than 0, or `nil` for no requirement

**Example:**

```clojure
(update-initial-margin 0.7)
(println INITIAL-MARGIN)
;; output:
0.7
```

### `MAINTENANCE-MARGIN`

Once the portfolio has borrowed cash or shares, the portfolio margin is checked every trading day. If it falls below the maintenance margin, all positions are closed and the run ends (a margin call). The default value is `0.25`.

**Example:**

```clojure
(println MAINTENANCE-MARGIN)
;; output:
0.25
```

### `update-maintenance-margin`

This function updates the maintenance margin.

**Parameters:**

- `num` - the new maintenance margin, greater than 0

**Example:**

```clojure
(update-maintenance-margin 0.2)
(println MAINTENANCE-MARGIN)
;; output:
0.2
```

## Transaction Cost & Loan Interest

### `TRANSACTION-COST`

The commission charged when an order fills, as a fraction of the trade value. It is charged on buys and sells alike and paid from cash. The default value is `0.0`.

**Example:**

```clojure
(println TRANSACTION-COST)
;; output:
0.0
```

### `update-transaction-cost`

This function updates the transaction cost.

**Parameters:**

- `num` - the new transaction cost, within the range [0, 1). 0 switches commission off.

**Example:**

```clojure
(update-transaction-cost 0.001) ;; 0.1% of the trade value
(println TRANSACTION-COST)
;; output:
0.001
```

### `INTEREST-RATE`

The simple annual interest rate charged on loans. Interest is deducted from cash every trading day, at 1/252 of the annual rate on the loan balance. The default value is `0.0`.

**Example:**

```clojure
(println INTEREST-RATE)
;; output:
0.0
```

### `update-interest-rate`

This function updates the interest rate.

**Parameters:**

- `num` - the new interest rate, within the range [0, 1). 0 switches interest off.

```clojure
(update-interest-rate 0.05)
(println INTEREST-RATE)
;; output:
0.05

(update-interest-rate 1.5)
;; output:
Failed: The interest rate needs to be within the range of [0,1).
```

## Dividends

### `REINVEST-DIVIDENDS`

When `true` (the default), dividends are reinvested in the same security: the value of a holding follows the security's total return, and the number of shares grows on dividend days. When `false`, dividends are paid into cash, and the number of shares changes only on splits. A short position pays the dividend instead of receiving it.

With dividends paid to cash, splits are recognised only if the main dataset has the CRSP `CFACPR` column; otherwise a split is paid out as cash too.

### `update-reinvest-dividends`

This function switches dividend reinvestment on or off.

```clojure
(update-reinvest-dividends false) ;; pay dividends to cash
```

## Output Files

### `OUTPUT-DIR`

The directory the records are written to as CSV files during a run: `out_order_record.csv`, `out_portfolio_value_record.csv` and `out_evaluation_report.csv`. By default it is `nil`, and no files are written.

### `update-output-dir`

This function sets the output directory, or `nil` for none. Call it before `init-portfolio`, which opens the files.

```clojure
(update-output-dir "out")
(init-portfolio "1990-01-02" 10000)
```

## Fundamentals

### `FUNDAMENTALS-MAX-AGE-MONTHS`

How many months after its period end a [fundamental](/posts/fundamentals/) value stays usable. An older value is treated as missing, because the company has stopped reporting or the data has no newer period. The default value is `18`: long enough to bridge the wait for an annual report, which can come three months after a fiscal year that itself covers twelve.

It applies to the Yahoo fundamentals. Compustat-style data has its own limit, `MERGE-MAX-AGE-MONTHS` (6).

### `update-fundamentals-max-age`

This function sets the maximum age, in months, greater than zero.

```clojure
(update-fundamentals-max-age 12)
(println FUNDAMENTALS-MAX-AGE-MONTHS)
;; output:
12

(update-fundamentals-max-age 0)
;; output:
Failed: The maximum age needs to be greater than zero.
```

## Indicator Parameters

These parameters are used by the corresponding [indicator](/posts/indicators/) functions. They can all be changed with `CHANGE-{parameter name}`; for example, `(CHANGE-EMA-CYCLE 15)` changes `EMA-CYCLE`.

### `EMA-CYCLE`

Cycle of the EMA. By default, 20.

### `MACD-SIGNAL`

Cycle of the MACD signal line. By default, 9.

### `MACD-SHORT`

Cycle of the MACD short line. By default, 12.

### `MACD-LONG`

Cycle of the MACD long line. By default, 26.

### `RSI-CYCLE`

Cycle of the RSI. By default, 14.
