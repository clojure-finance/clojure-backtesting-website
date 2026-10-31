{:title "Data and Trading Rules"
 :date "2021-01-20"
 :layout :post
 :section "Learn"
 :lead "Where the data comes from, its columns, the exact rules for filling orders, and how the data cache works."
 :toc true}

## About the Datasets

The backtester reads two kinds of data: a **main** dataset of daily prices, one row per security and trading day, and optionally **fundamentals**, values from company reports. They can come from:

| Source | Prices | Fundamentals | Load with |
| ------ | ------ | ------------ | --------- |
| The 1990 sample, `resources/sample-data` | Three securities, January to March 1990 | A Compustat-style dataset | `load-dataset` |
| The Yahoo sample, `resources/sample-yahoo` | Five securities, 2022 to 2024 | Point-in-time, like real Yahoo data | `load-yahoo-dataset`, after building it |
| CRSP and Compustat, or your own files | Converted with `clojask-script` | Compustat-style, joined onto the rows | `load-dataset` |
| Yahoo Finance | Downloaded and built by the library | Point-in-time, through the [Fundamentals](/posts/fundamentals/) functions | `load-yahoo-dataset` |

### Sample datasets

The library includes a small sample dataset in `resources/sample-data`, with three securities over January to March 1990 and a matching Compustat-style dataset (see [Get Started](/posts/get-started/#the-sample-data)). It is the quickest way to try every function:

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)
(load-dataset "resources/sample-data/compustat" "compustat") ;; optional
```

The second sample, `resources/sample-yahoo`, holds synthetic Yahoo Finance downloads of five securities over 2022 to 2024, with financial statements and earnings dates; see [Try it offline](/posts/yahoo-data/#try-it-offline).

### CRSP and Compustat

The backtester is designed for daily security data in the style of **CRSP**, the Center for Research in Security Prices. Definitions for each CRSP column can be found [here](/posts/docs/data_descriptions_guide_0.pdf).

**Compustat** data (company fundamentals) can be loaded as a supplementary dataset. Definitions for each Compustat column can be found [here](/posts/docs/FundamentalsAnnual.pdf).

CRSP and Compustat are licensed data, available through providers such as [WRDS](https://wrds-www.wharton.upenn.edu/), and are not distributed with the library.

### Yahoo Finance

The library can download daily prices, dividends, splits, financial statements and earnings dates from Yahoo Finance and build them into a main dataset in the CRSP layout, plus point-in-time fundamentals:

```clojure
(download-yahoo! "data/tech" ["AAPL" "MSFT" "NVDA" "GOOGL" "SPY"] :start "2015-01-01")
(build-yahoo-dataset! "data/tech")
(load-yahoo-dataset "data/tech")
```

The data is free and covers any ticker Yahoo quotes, but it has gaps CRSP does not: no delisted companies, and only a few years of statements. [Yahoo Finance Data](/posts/yahoo-data/) explains the pipeline and its limits.

### Using your own data

Datasets have to be converted into the backtester's layout first, with the [`clojask-script`](https://github.com/clojure-finance/clojure-backtesting/tree/master/clojask-script) tool in the repository. Its README explains the column mappings. Then load them by directory:

```clojure
(load-dataset "/path/to/CRSP" "main" add-aprc) ;; change the first argument to your local path
(load-dataset "/path/to/Compustat" "compustat")
```

`add-aprc` adds an adjusted price column to the main dataset. It rewrites the dataset files in place and can take hours on the full CRSP data the first time; later loads detect the added columns and skip this step.

The Compustat dataset does not replace the main dataset. Instead, each day's rows of the main dataset are joined with the matching Compustat rows, so the maps returned by `get-info`, `get-info-map`, `get-permno-info` and the other data functions also contain the Compustat columns (as strings) where a filing matches. For each security, the backtester uses the latest filing that was public on that day:

- its date (period end) is on or before the day and at most `MERGE-MAX-AGE-MONTHS` (6) months old, and
- when the dataset has an `rdq` (report date) column, the report date is also on or before the day.

So a strategy only sees fundamentals that had been reported by then.

### Essential column names

Each row of the main dataset is a map keyed by the column names as keywords. The backtester uses these columns:

- `:PERMNO` : security identifier

  **This documentation uses *permno* and *security* interchangeably.**

- `:PRC` : closing price

- `:RET` : daily return, including distributions

- `:APRC`, `:INIT-PRICE`, `:CUM-RET` : adjusted price and its helper columns, added by `add-aprc`

Optional columns:

- `:OPENPRC` : opening price, if you trade at the open (see [Parameters](/posts/parameters/#trading-price))
- `:TICKER` : ticker symbol, used by `permno-tic` and `tic-permno`
- `:CFACPR` : CRSP's cumulative price adjustment factor, used to recognise splits when dividends are not reinvested
- `:BIDLO`, `:ASKHI` : daily low and high, needed by `parabolic-SAR`, `ATR` and `keltner-channel`

A dataset built from Yahoo Finance has all of these, already furnished with the adjusted prices, plus `:VOL` (volume), `:RETX` (the return without dividends), `:DIVAMT` (the dividend per share going ex that day), `:CFACSHR` and `:CURRENCY`. The backtester does not need them, but your strategy can read them like any other column. See [The main dataset](/posts/yahoo-data/#the-main-dataset).

**Remark:** the trading price is the **closing price** by default.

## Trading Logic

As introduced in [moving the time pointer](/posts/api/#move-through-time), the backtester has a global clock. Your strategy normally runs between two calls of `next-date`. The figure below illustrates the logic of trading with an example.

![On 1990-01-02 the strategy orders 50 shares, which stays pending. next-date moves to 1990-01-03, where the order fills at the close of $50.81.](/img/order-timeline.svg)

Say today is 2022-10-24 and you order 10 shares of security xx. The order is not filled immediately. Instead, it becomes a pending order, which the backtester fills on the next call of `next-date`, at that day's **closing price**. So your portfolio and the order record do not change right after `order`; they change on the next trading day.

- If the security has no price on the next day, the order stays pending and fills on the first day it does have a price, until it expires after `ORDER-EXPIRATION` (3) trading days. Pass `:expiration n` to `order` to change this for one order.
- On a given day, orders that reduce a position fill before orders that add to one, so sale proceeds can pay for purchases.
- Buying for more than your cash borrows the difference (a loan). Selling shares you do not hold borrows the shares (a short position). Both must respect the margin requirements on the [Parameters](/posts/parameters/#margin-requirements) page; if the portfolio margin falls below the maintenance margin, every position is closed.
- Loans are charged interest every trading day, at the annual `INTEREST-RATE` (0 by default).
- Dividends are reinvested in the same security by default; they can be paid into cash instead (see [Parameters](/posts/parameters/#dividends)).
- A holding whose security has had no price for `MISSING-DAYS-LIMIT` (10) trading days in a row is treated as delisted, and its last value is booked as cash.

When your strategy is done, call `end-order` to close every position at today's close.

## Cache Mechanism

The backtester reads the dataset from disk and needs to access it frequently. To avoid repeating file reads, it caches the market information of days you have looked at. By default the cache holds 60 days, and you can change this depending on the memory of your computer. For example, we recommend 30 for an 8 GB computer and 60 for a 16 GB one.

```clojure
(CHANGE-CACHE-SIZE 30) ;; set the cache size to 30 days
```

When the cache is full, the backtester removes the oldest information, determined by the date of the information rather than when it was added. Data that is not in the cache is read from the file again, which takes longer. So looking back within the cache size is fast, while looking further back can be significantly slower.
