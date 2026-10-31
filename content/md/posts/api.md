{:title "Core API"
 :date "2022-10-16"
 :layout :post
 :section "API Reference"
 :lead "Load data, start a portfolio, place orders, move the clock and read market data."
 :toc true}

These are the functions every strategy uses. The examples run on the bundled sample dataset (see [Get Started](/posts/get-started/#the-sample-data)), mostly with the clock at 1990-01-08. Functions for Yahoo Finance data, fundamentals and portfolio strategies have their own pages: [Yahoo Finance Data](/posts/yahoo-data/), [Fundamentals](/posts/fundamentals/) and [Strategy Helpers](/posts/strategy/).

**How to read this page:** each function has a table of its arguments, then what it returns and an example. Arguments in [square brackets] are optional. Lines starting with `=>` are what you type at the REPL; the lines below them are what it prints or returns.

## Load Data

### `load-dataset`

Load a dataset that has been converted into the backtester's layout (see [Data and Trading Rules](/posts/user-guide/#using-your-own-data)).

| Argument            | Type                 | Function                          | Remarks                                                      |
| ------------------- | -------------------- | --------------------------------- | ------------------------------------------------------------ |
| `dir`               | String               | The path of the dataset directory |                                                              |
| `role`              | "main" / "compustat" | Whether this is the main (CRSP-style) dataset or the supplementary Compustat-style dataset | Load at most one of each. |
| [`func`]            | Function             | A function that augments the dataset when it is loaded | Pass `add-aprc` for the main dataset: it adds the adjusted price columns that `init-portfolio` requires. It rewrites the files in place the first time and does nothing afterwards. |

**Return**

"Date range: {start date} ~ {end date}"

**Example**

```clojure
=> (load-dataset "resources/sample-data/main" "main" add-aprc)
The dataset is already furnished by add-aprc. No more modification is needed.
"Date range: 1990-01-02 ~ 1990-03-30"
=> (load-dataset "resources/sample-data/compustat" "compustat")
"Date range: 1989-09-30 ~ 1989-12-31"
```

To load a dataset built from Yahoo Finance, use [`load-yahoo-dataset`](/posts/yahoo-data/#load-yahoo-dataset) instead: it loads the prices as the main dataset, already furnished with the adjusted prices, together with the fundamentals.

## Start a Portfolio

### `init-portfolio`

Set the starting date and capital. Calling it again restarts the backtester: the portfolio, all records, indicators and automations are reset, and orders that have not filled yet are dropped.

| Argument  | Type   | Function                     | Remarks                                                      |
| --------- | ------ | ---------------------------- | ------------------------------------------------------------ |
| `date`    | String | The starting date            | If this date is missing in the dataset (e.g. it is a weekend), the nearest earlier date is used instead. If there is none, the nearest later date. It cannot be the last date of the dataset. |
| `capital` | Number | The initial capital          | Given in the form of cash                                    |

**Return**

"Date: {actual starting date (may be different from `date`)} Cash: ${`capital`}"

**Example**

```clojure
=> (init-portfolio "1990-01-06" 10000) ;; a Saturday
"Date: 1990-01-05 Cash: $10000"
```

**Remarks**

- Set parameters such as `update-output-dir` or `update-reinvest-dividends` before calling `init-portfolio` (see [Parameters](/posts/parameters/)).
- More details about the portfolio can be found [here](/posts/portfolio/).

## Place Orders

### `order`

Buy or sell a number of shares of a security. The order fills at the closing price of the next trading day on which the security has a price (see [Trading Logic](/posts/user-guide/#trading-logic)).

| Argument        | Type    | Function                                                     | Remarks                                                      |
| --------------- | ------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `security`      | String  | The target security                                          |                                                              |
| `quantity`      | Number  | Positive means buy, negative means sell                      | The trading amount, or the target holding when `:remaining` is true. |
| [`:expiration`] | Integer | Number of trading days the order stays pending               | By default, `ORDER-EXPIRATION` (3).                          |
| [`:remaining`]  | Boolean | Whether `quantity` is the holding to end up with, rather than the amount to trade | By default, false. `(order "10001" 0 :remaining true)` closes the position. |
| [`:leverage`]   | Boolean | Whether the order may borrow cash (buying beyond your cash) or shares (short selling) | By default, `LEVERAGE` (true). Borrowing must respect `INITIAL-MARGIN`. |
| [`:print`]      | Boolean | Whether to print the order when it fills                     | By default, `PRINT` (false).                                 |
| [`:direct`]     | Boolean | Whether to write the order to `out_order_record.csv` when it fills | By default, `DIRECT` (true). Files are written only when an output directory is set with `update-output-dir`. |

**Return**

All pending orders, as a map keyed by [expiry date, security].

**Example**

```clojure
=> (order "10001" 10)
{["1990-01-11" "10001"] {:place "1990-01-08", :expire "1990-01-11", :permno "10001", :quantity 10, :remaining false, :leverage true, :print false, :direct true}}

=> (order "10002" 5 :remaining true :expiration 5) ;; hold 5 shares of 10002
{["1990-01-11" "10001"] {...}, ["1990-01-15" "10002"] {:place "1990-01-08", :expire "1990-01-15", :permno "10002", :quantity 5, :remaining true, :leverage true, :print false, :direct true}}
```

### `end-order`

Call this at the end of your strategy. It closes every position at today's closing price, updates the evaluation report once more, closes the CSV files and ends the run; after it, `order` and `next-date` do nothing until `init-portfolio` is called again. A security with no price today is booked as cash at its last value.

The run also ends by itself on the last date of the dataset, when the total value drops below zero, or on a margin call (see [Parameters](/posts/parameters/#margin-requirements)).

**Arguments:** none

**Example**

```clojure
=> (end-order)
true
=> (next-date)
nil
```

## Move Through Time

The backtester has a date pointer that acts as its global clock. You can move it forward but not back, and you only see market information up to "today", never the future (see [Key Concepts](/posts/concepts/#the-clock-today-and-the-date-pointer)). The functions below read or move the date pointer.

### `get-date`

Get today's date.

**Arguments:** none

**Return**

A string representing the current date.

**Example**

```clojure
=> (get-date)
"1990-01-05"
```

### `next-date`

Move the date pointer forward to the next trading day in the dataset, skipping weekends and holidays. On the new day it revalues the holdings, charges loan interest, checks the margin, fills pending orders and runs automations.

**Arguments:** none

**Return**

The new date. `nil` once the run has ended (through `end-order`, a margin call, or reaching the last date of the dataset).

**Example**

```clojure
=> (next-date)
"1990-01-08"
```

### `get-next-date`

Get the next trading day without moving the date pointer.

**Arguments:** none

**Return**

A date string, or `nil` on the last date of the dataset.

**Example**

```clojure
=> (get-date)
"1990-01-05"
=> (get-next-date)
"1990-01-08"
```

### `get-prev-n-date`

Get the date n trading days before the current date (skipping weekends and holidays).

| Argument | Type             | Function                    | Remarks                                                |
| -------- | ---------------- | --------------------------- | ------------------------------------------------------ |
| `n`      | Positive Integer | Number of days to look back |                                                        |

**Return**

A date string, or `nil` if the search passes the first date of the dataset.

**Example**

```clojure
=> (get-date)
"1990-01-08"
=> (get-prev-n-date 3)
"1990-01-03"
```

### `date-after-n-trading-days`

Get the date n trading days after a given date.

| Argument | Type             | Function                     | Remarks |
| -------- | ---------------- | ---------------------------- | ------- |
| `date`   | String           | The date to count from       |         |
| `n`      | Positive Integer | Number of days to look ahead |         |

**Return**

A date string, or the last date of the dataset if fewer than n trading days remain.

**Example**

```clojure
=> (date-after-n-trading-days "1990-01-08" 3)
"1990-01-11"
```

## Read Market Data

Each row is a map keyed by the dataset's column names (see [Essential column names](/posts/user-guide/#essential-column-names)). When a Compustat dataset is loaded, the rows also contain the Compustat columns of the matching filing.

### `get-info`

Get all the market information for today, as a sequence of maps. There is also `get-info-map`, a format better suited to looking up a specific security.

**Arguments:** none

**Return**

A sequence of maps, one per security that trades today.

**Example**

```clojure
=> (get-info)
[{:ASKHI 50.58, :CUM-RET -0.005342844410495502, :date "1990-01-08", :INIT-PRICE 50.67, :OPENPRC 50.49, :TICKER "AAA", :APRC 50.4, :BIDLO 50.28, :RET 5.955926146516966E-4, :CFACPR 1.0, :PRC 50.4, :PERMNO "10001"} {:ASKHI 116.79, :CUM-RET -0.02671833211289444, :date "1990-01-08", :INIT-PRICE 118.72, :OPENPRC 116.37, :TICKER "BBB", :APRC 115.58999999999999, :BIDLO 114.99, :RET -0.011544381734222675, :CFACPR 2.0, :PRC 115.59, :PERMNO "10002"} {:ASKHI 20.11, :CUM-RET -0.013965314248641295, :date "1990-01-08", :INIT-PRICE 20.19, :OPENPRC 20.03, :TICKER "CCC", :APRC 19.91, :BIDLO 19.9, :RET -0.00945273631840804, :CFACPR 1.0, :PRC 19.91, :PERMNO "10003"}]
```

### `get-info-map`

Get all the market information for today, as a map from security to its information.

**Arguments:** none

**Return**

A map whose keys are all the securities available today.

**Example**

```clojure
=> (get-info-map)
{"10001" {:ASKHI 50.58, :CUM-RET -0.005342844410495502, :date "1990-01-08", :INIT-PRICE 50.67, :OPENPRC 50.49, :TICKER "AAA", :APRC 50.4, :BIDLO 50.28, :RET 5.955926146516966E-4, :CFACPR 1.0, :PRC 50.4, :PERMNO "10001"}, "10002" {...}, "10003" {...}}
```

### `available-permnos`

Get all securities available today.

**Arguments:** none

**Return**

A sequence of permno strings.

**Example**

```clojure
=> (available-permnos)
("10001" "10002" "10003")
```

### `get-permno-info`

Get the information for a security.

| Argument | Type   | Function             | Remarks |
| -------- | ------ | -------------------- | ------- |
| [`date`] | String | The date to look up  | By default, today. |
| `permno` | String | The security         |         |

**Return**

A map of information if the security has data on that date, otherwise `nil`.

**Example**

```clojure
=> (get-permno-info "10001")
{:ASKHI 50.58, :CUM-RET -0.005342844410495502, :date "1990-01-08", :INIT-PRICE 50.67, :OPENPRC 50.49, :TICKER "AAA", :APRC 50.4, :BIDLO 50.28, :RET 5.955926146516966E-4, :CFACPR 1.0, :PRC 50.4, :PERMNO "10001"}

=> (get-permno-info "99999")
nil
```

### `get-permno-price`

Like `get-permno-info`, but get only the price of the security (the closing price, or the opening price if `PRICE-KEY` is changed).

| Argument | Type   | Function             | Remarks |
| -------- | ------ | -------------------- | ------- |
| [`date`] | String | The date to look up  | By default, today. |
| `permno` | String | The security         |         |

**Return**

A number, or `nil` if the security has no data on that date.

**Example**

```clojure
=> (get-permno-price "10001")
50.4
=> (get-permno-price "1990-01-03" "10001")
50.81
```

### `get-permno-by-key`

Like `get-permno-info`, but get the value of one key.

| Argument | Type    | Function             | Remarks                                           |
| -------- | ------- | -------------------- | ------------------------------------------------- |
| [`date`] | String  | The date to look up  | By default, today.                                |
| `permno` | String  | The security         |                                                   |
| `key`    | Keyword | The key              | One of the dataset's columns                      |

**Return**

The value, of a type depending on the key, if the map contains the key, otherwise `nil`.

**Example**

```clojure
=> (get-permno-by-key "10001" :TICKER)
"AAA"
=> (get-permno-by-key "10001" :None)
nil
```

### `permno-tic` and `tic-permno`

Translate between security identifiers and ticker symbols, using today's data (needs a `:TICKER` column).

**Example**

```clojure
=> (permno-tic "10001")
["AAA"]
=> (tic-permno "AAA")
["10001"]
```

### `get-prev-n-days`

Get the information of the past n trading days (not including today).

| Argument | Type             | Function                        | Remarks                                                      |
| -------- | ---------------- | ------------------------------- | ------------------------------------------------------------ |
| [`n`]    | Positive Integer | The number of days to look back | If not given, return a lazy sequence of all previous days.<br>The result may be shorter than `n` if it reaches the first date of the dataset. |

**Return**

A sequence (length <= n) of sequences of maps (one per security), in descending time order, i.e. the most recent date first.

**Example**

```clojure
=> (map #(map :PERMNO %) (get-prev-n-days 2))
(("10001" "10002" "10003") ("10002" "10001" "10003"))

;; a common way to get the information of the past n days including today:
=> (conj (get-prev-n-days (- n 1)) (get-info))
```

### `get-prev-n-days-map`

Like `get-prev-n-days`, but each day is a map from security to its information, as in `get-info-map`.

**Example**

```clojure
=> (map keys (get-prev-n-days-map 2))
(("10001" "10002" "10003") ("10002" "10001" "10003"))
```

### `get-permno-prev-n-days`

Get the information of a security for the past n trading days (not including today).

| Argument | Type             | Function                        | Remarks                                                      |
| -------- | ---------------- | ------------------------------- | ------------------------------------------------------------ |
| `permno` | String           | The security                    |                                                              |
| `n`      | Positive Integer | The number of days to look back | If the security is absent on some of these days, the result is shorter than `n`. |

**Return**

A sequence (length <= n) of maps, in descending time order, i.e. the most recent date first.

**Example**

```clojure
=> (get-permno-prev-n-days "10001" 2)
({:ASKHI 50.55, :CUM-RET -0.00593825973025941, :date "1990-01-05", :INIT-PRICE 50.67, :OPENPRC 50.06, :TICKER "AAA", :APRC 50.36999999999999, :BIDLO 49.91, :RET 0.0029868578255674016, :CFACPR 1.0, :PRC 50.37, :PERMNO "10001"} {:ASKHI 50.89, :CUM-RET -0.0089206657583795, :date "1990-01-04", :INIT-PRICE 50.67, :OPENPRC 50.83, :TICKER "AAA", :APRC 50.22, :BIDLO 50.05, :RET -0.011611887423735534, :CFACPR 1.0, :PRC 50.22, :PERMNO "10001"})

;; a common way to get the information of the past n days including today:
=> (conj (get-permno-prev-n-days "10001" (- n 1)) (get-permno-info "10001"))
({today's info} {yesterday's info} ...)
```
