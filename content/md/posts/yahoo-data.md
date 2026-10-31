{:title "Yahoo Finance Data"
 :date "2026-10-31"
 :layout :post
 :section "Learn"
 :lead "Download prices, dividends, splits, financial statements and earnings dates for any ticker, turn them into a dataset the backtester loads, and know where the data falls short."
 :toc true}

CRSP and Compustat are the reference data for academic backtests, but they need a licence. Since version 1.1.2 the library can also use [Yahoo Finance](https://finance.yahoo.com/), through the [clj-yfinance](https://github.com/clojure-finance/clj-yfinance) library: free, with no API key, for any ticker Yahoo quotes. You get daily prices with dividends and splits, plus the financial statements and earnings dates behind [point-in-time fundamentals](/posts/fundamentals/).

## The three steps

```clojure
(download-yahoo! "data/tech" ["AAPL" "MSFT" "NVDA" "GOOGL" "SPY"] :start "2015-01-01")
(build-yahoo-dataset! "data/tech")
(load-yahoo-dataset "data/tech")
(init-portfolio "2023-03-01" 100000)
```

| Step | What it does | Needs the network |
| ---- | ------------ | ----------------- |
| [`download-yahoo!`](#download-yahoo) | Saves Yahoo's answers, unchanged, under `data/tech/raw`: one snapshot file per security and download | Yes |
| [`build-yahoo-dataset!`](#build-yahoo-dataset) | Turns the snapshots into a main dataset (`main/`), the fundamentals (`fundamentals.edn`) and a description (`dataset.edn`) | No |
| [`load-yahoo-dataset`](#load-yahoo-dataset) | Loads the prices as the main dataset and the fundamentals | No |

Building is separate from downloading, so you can rebuild with other options as often as you like without asking Yahoo again. [`yahoo-dataset!`](#yahoo-dataset) runs all three steps in one call.

After loading, everything on this site works as with any other dataset. The security id (the *permno*) is the ticker, so you write `(get-permno-price "AAPL")` and `(order "MSFT" 10)`.

> **Tip:** `lein repl` in a clone of the library has these functions loaded. In your own namespace, require `clojure-backtesting.yahoo` and `clojure-backtesting.fundamentals`, as in [Get Started](/posts/get-started/#use-the-library-in-your-own-namespace).

## Try it offline

The repository includes synthetic snapshots in `resources/sample-yahoo`, in exactly the shape a real download has. They let you try the whole pipeline without a network connection, and this page and the [Fundamentals](/posts/fundamentals/) page use them for every example. The companies are made up, each to exercise one feature:

| Ticker | What is special about it |
| ------ | ------------------------ |
| ALFA | Pays a quarterly dividend, splits 4-for-1 on 2024-08-01, reports after the close. Downloaded twice (2024-06-01 and 2025-01-02), and one quarter was restated in between |
| BETA | Lost money through 2022. Yahoo has no report dates for its recent periods, so they are estimated |
| GAMA | Trades in US dollars but reports in euros, before the market opens |
| DLTA | Prices only, no statements |
| IDX | An index, to compare strategies against |
| ZZZZ | Not found by Yahoo, like a delisted ticker |

The prices cover the weekdays of 2022 to 2024. Build them into `target/sample-yahoo` and load the result:

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
```

```text
Warning: ZZZZ has no prices: No data found, symbol may be delisted
Warning: BETA: 7 report dates are estimated from the reporting lag
"Date range: 2022-01-03 ~ 2025-01-01; Fundamentals: 5 securities, 33 reports, 1 FX series"
```

The warnings are expected: they say what the build could not do exactly. ZZZZ is left out, and seven of BETA's reports are dated by an assumed reporting lag, because Yahoo gave no date for them (see [Report dates](#report-dates)).

```clojure
(init-portfolio "2024-11-15" 100000)
(available-permnos)
;; => ("ALFA" "BETA" "DLTA" "GAMA" "IDX")
```

## Downloading

`download-yahoo!` fetches each ticker in turn, two at a time by default, and prints a line per ticker saying whether it worked. For each one it saves:

- the daily bars (open, high, low, close, volume) with dividends and splits, from `:start` up to today;
- for companies (Yahoo's instrument type `EQUITY`): annual, quarterly and trailing twelve-month values of the statement fields in [`default-fields`](#default-fields), Yahoo's earnings history and its earnings announcement dates.

Indices, ETFs and currencies have prices only. After the tickers, it fetches the exchange rates needed to convert statements reported in another currency than the one the security trades in, such as `EURUSD=X` for a company that reports in euros and trades in dollars.

It returns what happened:

```clojure
{:downloaded ["AAPL" "MSFT" ...]   ; tickers fetched now
 :skipped    []                    ; already downloaded today
 :failed     {"XYZ" {...}}         ; tickers whose prices failed, with Yahoo's error
 :fx         [["EUR" "USD"] ...]}  ; exchange rates fetched
```

### Resuming and updating

A ticker that was already downloaded today (in UTC) is skipped, so when a download is interrupted, run the same call again and it carries on where it stopped. `:force? true` downloads again anyway.

A ticker that **failed**, for example because Yahoo rate-limited the request, is saved too, with its error, so a rerun on the same day skips it as well. Retry the failures with `:force?`:

```clojure
(def result (download-yahoo! "data/tech" ["AAPL" "MSFT" "NVDA" "GOOGL" "SPY"] :start "2015-01-01"))
(download-yahoo! "data/tech" (keys (:failed result)) :start "2015-01-01" :force? true)
```

`:failed` lists the tickers whose prices failed. A ticker whose prices arrived but whose statements did not is not in it; its progress line names the parts with errors.

Each download adds a new snapshot instead of replacing the old one, and the build merges them. That matters for fundamentals: Yahoo only shows about four fiscal years and five quarters at a time, so **downloading again every quarter makes the history grow** beyond what Yahoo shows on any one day.

> **Watch out:** Yahoo's endpoints are unofficial. They are undocumented, rate-limited, and may change. Keep the defaults of two tickers at a time with 300 ms between requests, and check Yahoo's terms before using the data commercially.

### Download options

| Option | Default | Meaning |
| ------ | ------- | ------- |
| `:start` | all history | First date of the price history, `"yyyy-MM-dd"`. Prices always run up to today; end the dataset earlier with the build's `:end` |
| `:fundamentals?` | `true` | Also fetch statements and earnings dates. `false` downloads prices only, which is quicker |
| `:fields` | `default-fields` | Yahoo statement fields to fetch, or `:all` for every field clj-yfinance knows |
| `:concurrency` | `2` | Tickers downloaded at the same time |
| `:pause-ms` | `300` | Pause between two requests of one thread, in milliseconds |
| `:force?` | `false` | Download again even if done today |

Why do prices always run up to today? Yahoo divides old prices by every split up to today, but only returns the splits inside the requested range. A range that ended earlier could not be turned back into the prices that were actually traded.

## Building

`build-yahoo-dataset!` reads every snapshot under `<dir>/raw` and writes three things into `<dir>`, or into `:out-dir` when given:

| Output | Contents |
| ------ | -------- |
| `main/` | The main dataset: one row per security and trading day, in the CRSP layout, already furnished with the adjusted prices |
| `fundamentals.edn` | The point-in-time fundamentals and the exchange rates |
| `dataset.edn` | What was built: coverage per security, the options and the warnings. Read it back with [`dataset-info`](#dataset-info) |

It prints the warnings and returns the contents of `dataset.edn`.

### The main dataset

The rows have the CRSP columns the backtester knows, so orders, dividends, splits and indicators work exactly as with CRSP data:

| Column | Meaning |
| ------ | ------- |
| `:PERMNO`, `:TICKER` | The ticker, used as the security id |
| `:PRC`, `:OPENPRC`, `:BIDLO`, `:ASKHI` | Close, open, low and high, as traded on the day |
| `:VOL` | Volume, in shares of the day |
| `:DIVAMT` | Cash dividend per share going ex on this day, as paid |
| `:RET`, `:RETX` | Total return (dividends included, at the ex-date) and the return without dividends |
| `:CFACPR`, `:CFACSHR` | The product of the later splits: how many of today's shares one share of that day has become |
| `:CURRENCY` | The quote currency, such as `"USD"` |
| `:APRC`, `:INIT-PRICE`, `:CUM-RET` | The adjusted price and its helper columns, so `add-aprc` is not needed |

Trading dates are taken in the exchange's time zone. The download day itself is left out, because its trading session may not have ended.

### Prices as traded

Yahoo quotes every old price divided by all later splits, so on its charts ALFA never cost more than about $120. The build restores the prices as they were traded, and records the split in `:CFACPR`. On the day of ALFA's 4-for-1 split the price drops to a quarter, but the total return `:RET` and the adjusted price `:APRC` do not jump:

```clojure
(for [date ["2024-07-30" "2024-07-31" "2024-08-01" "2024-08-02"]]
  (select-keys (get-permno-info date "ALFA") [:date :PRC :CFACPR :RET :DIVAMT :APRC]))
```

```text
({:date "2024-07-30", :PRC 393.5576, :CFACPR 4.0, :RET -0.008683018442094403, :DIVAMT 0.0, :APRC 397.58620611357276}
 {:date "2024-07-31", :PRC 382.4636, :CFACPR 4.0, :RET -0.02818901223099235, :DIVAMT 0.0, :APRC 386.37864368656335}
 {:date "2024-08-01", :PRC 93.9474, :CFACPR 1.0, :RET -0.017450026616911973, :DIVAMT 0.0, :APRC 379.6363260700265}
 {:date "2024-08-02", :PRC 93.4705, :CFACPR 1.0, :RET -0.005076244792298623, :DIVAMT 0.0, :APRC 377.7091991468461})
```

Dividends are restated the same way. ALFA pays $0.40 a quarter before the split and $0.10 after it, the same amount per original share:

```clojure
(select-keys (get-permno-info "2024-05-16" "ALFA") [:date :PRC :DIVAMT :CFACPR :RET :RETX])
;; => {:date "2024-05-16", :PRC 370.6972, :DIVAMT 0.4, :CFACPR 4.0, :RET -0.0017431294338166037, :RETX -0.0028191352571600437}
(select-keys (get-permno-info "2024-08-15" "ALFA") [:date :PRC :DIVAMT :CFACPR :RET :RETX])
;; => {:date "2024-08-15", :PRC 98.8489, :DIVAMT 0.1, :CFACPR 1.0, :RET 0.020163269177633003, :RETX 0.019132269066284957}
```

Traded prices matter because a strategy that orders 100 shares should pay what 100 shares cost at the time. They also mean that indicators, which use the raw price, see a split as a crash, as with CRSP data; see [Prices: raw and adjusted](/posts/concepts/#prices-raw-and-adjusted).

### Report dates

A statement value is only useful in a backtest together with the date it became public. The build gives every reported period a **public date**, the first trading day a strategy may use its numbers, from the best source available:

| Source | Used when | `:report-date-source` |
| ------ | --------- | --------------------- |
| Yahoo's earnings history | Yahoo pairs the period with its report date | `:earnings-history` |
| Yahoo's earnings announcements | The first earnings announcement within 100 days after the period end | `:announcement` |
| An assumed reporting lag | Neither is known: 45 days after a quarter, 90 after a fiscal year (also for the quarter that ends a fiscal year) | `:lag` |

Some details:

- An announcement **after the market close** counts from the next trading day. So does one without a time, to be safe. The close is 16:00 local time for most exchanges, and earlier for Tokyo, Shanghai, Seoul, Taipei, Mumbai, Karachi and Manila. An announcement before the open, or at a known time before the close, counts from the same day.
- A public date on a weekend or holiday moves to the next trading day.
- The lag is roughly the SEC's filing deadline. For US large caps it is later than the real report, so estimated dates err on the safe side. For markets that report more slowly, pass a longer `:report-lag-days`.

The [Key Concepts](/posts/concepts/#fundamentals-as-they-were-known) page has a diagram of a report date. [`fundamental-info`](/posts/fundamentals/#fundamental-info) tells you where a value's date came from.

### Snapshots and restatements

When a ticker has several snapshots, the build merges them. Values are rebased to one share basis across splits, so a quarter downloaded before ALFA's split and after it agree. When Yahoo has **restated** a value between two downloads, the build keeps the first version by default (`:vintage :first`), the one closest to what was originally reported. `:vintage :latest` keeps the newest. ALFA's net income for the fourth quarter of 2023 was restated in the sample:

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo-latest" :vintage :latest)
(load-yahoo-dataset "target/sample-yahoo-latest")
(init-portfolio "2024-03-01" 100000)
(fundamental "ALFA" :net-income :quarterly)
;; => 5.8801916250000015E7    with the default :vintage :first, 5.7648937500000015E7
```

Within a single download, values are as Yahoo shows them today, which may already include restatements.

### Build options

| Option | Default | Meaning |
| ------ | ------- | ------- |
| `:out-dir` | the input directory | Where to write `main/`, `fundamentals.edn` and `dataset.edn` |
| `:start`, `:end` | all dates | Keep only price rows within these dates, `"yyyy-MM-dd"` |
| `:report-lag-days` | `{:quarterly 45 :annual 90 :trailing 45}` | Days after the period end assumed when Yahoo has no report date |
| `:after-close-hour` | the exchange's close, 16 for most | Local hour from which an announcement counts from the next day |
| `:vintage` | `:first` | `:first` or `:latest`, for values Yahoo restated between downloads |
| `:allow-mixed-currencies?` | `false` | Build even though the securities trade in different currencies; see [Currencies](#currencies) |

A longer reporting lag pushes BETA's estimated dates later. With 60 days instead of 45, on 2024-11-15 its third quarter is not public yet, and the second quarter is the latest:

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo-lag"
                      :report-lag-days {:quarterly 60 :annual 120 :trailing 60})
(load-yahoo-dataset "target/sample-yahoo-lag")
(init-portfolio "2024-11-15" 100000)
(select-keys (fundamental-info "BETA" :net-income :quarterly) [:period-end :public-date :report-date-source])
;; => {:period-end "2024-06-30", :public-date "2024-08-29", :report-date-source :lag}
```

With the default lag, the same call returns the quarter ending 2024-09-30, public on 2024-11-14. `:start` and `:end` cut the price history:

```clojure
(:date-range (build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo-2024"
                                   :start "2024-01-01" :end "2024-06-30"))
;; => ["2024-01-01" "2024-06-28"]
```

A range without any prices is an error: `"No prices between 2030-01-01 and the end."`

### What was built

`dataset.edn` records the coverage of every security, how its report dates were found, and the warnings. Part of it for the sample:

```clojure
(get-in (dataset-info "target/sample-yahoo") [:securities "ALFA"])
```

```text
{:first-date "2022-01-03",
 :last-date "2025-01-01",
 :days 783,
 :currency "USD",
 :reporting-currency "USD",
 :snapshots 2,
 :fundamentals {:periods 13,
                :values 213,
                :restated 1,
                :report-sources {:earnings-history 9, :announcement 4}}}
```

ALFA has 13 reported periods with 213 values, one of them restated between its two downloads, and every report date came from Yahoo. The `:warnings` are the ones the build printed, as data:

```clojure
(:warnings (dataset-info "target/sample-yahoo"))
```

```text
[{:type :no-prices, :ticker "ZZZZ",
  :error {:type :http-error, :status 404, :message "No data found, symbol may be delisted"}}
 {:type :estimated-report-dates, :ticker "BETA", :periods 7}]
```

A third kind, `:missing-fx`, names an exchange rate that is needed but was not downloaded.

## Loading

`load-yahoo-dataset` loads `<dir>/main` as the main dataset and `<dir>/fundamentals.edn` as the fundamentals. A Compustat-style dataset loaded earlier is unloaded, because it belongs to another universe of securities. Then start a portfolio as usual:

```clojure
(load-yahoo-dataset "target/sample-yahoo")
;; => "Date range: 2022-01-03 ~ 2025-01-01; Fundamentals: 5 securities, 33 reports, 1 FX series"
(init-portfolio "2023-01-03" 100000)
```

From here, the [Fundamentals](/posts/fundamentals/) functions answer what was known on each day, and the [Strategy Helpers](/posts/strategy/) rank securities and rebalance to target weights. The [Value Strategy](/posts/examples/example-value-strategy/) example puts it all together.

## Currencies

**One currency per portfolio.** The backtester adds up cash and positions as plain numbers, so it cannot mix dollars and euros. The build refuses securities quoted in different currencies, and asks you to build one dataset per currency. `:allow-mixed-currencies? true` builds anyway, for strategies that never hold more than one of them at a time.

**Statements in another currency.** Many companies report in a different currency than the one their shares trade in, for example European companies listed in New York. Their values are converted into the trading currency at the previous day's exchange rate, since a day's exchange rate closes after many stock markets do. GAMA reports in euros and trades in dollars:

```clojure
(init-portfolio "2024-11-15" 100000)
(select-keys (fundamental-info "GAMA" :total-revenue) [:value :currency :reported-value :reported-currency])
;; => {:value 2.8602568584212475E9, :currency "USD", :reported-value 2.40873877504E9, :reported-currency "EUR"}
```

Without the exchange rate, such values are `nil` rather than wrong, and the build warns about the missing rate.

**Pence and cents.** London quotes most shares in pence (`GBp`), Johannesburg in cents. Prices stay as quoted, so `:PRC` is in pence, but fundamentals and ratios are stated in the major unit (pounds), and ratios such as P/E take the unit into account.

## Limits of the data

Free data has catches. Keep them in mind when you read a result:

- **Survivorship bias.** Yahoo only has securities that still trade. A universe chosen today leaves out the companies that failed or were taken over, which are often the ones a strategy would have lost money on. Treat results as optimistic.
- **Short fundamentals history.** Fundamentals start roughly three and a half years before the first download, while prices go back decades. Start a fundamentals strategy once its universe has data, or it trades on the few companies that report first. Downloading every quarter extends the history (see [Resuming and updating](#resuming-and-updating)).
- **Restated, not as first reported.** Within one download, values are as Yahoo has them today, possibly after later corrections.
- **Estimated report dates.** Where Yahoo has no date, a reporting lag is assumed. The build warns about each such security, and `fundamental-info` flags each such value.
- **Share classes.** For a few companies with several share classes, Yahoo counts shares in another class than the one quoted (Berkshire Hathaway's B shares in A equivalents). The library checks the share count against net income over EPS, and if they disagree more than twofold it warns and leaves out the ratios on market value.
- **Unofficial API.** See the warning under [Resuming and updating](#resuming-and-updating).

## Function reference

All of these are in `clojure-backtesting.yahoo`.

### `download-yahoo!`

`(download-yahoo! dir tickers & options)`

Downloads `tickers` (a sequence of strings, such as `["AAPL" "^GSPC" "BRK-B"]`) into `<dir>/raw`, plus the exchange rates their statements need. Options are listed under [Download options](#download-options). Returns `{:downloaded :skipped :failed :fx}`.

### `build-yahoo-dataset!`

`(build-yahoo-dataset! dir & options)`

Builds `main/`, `fundamentals.edn` and `dataset.edn` from the snapshots in `<dir>/raw`, offline. Options are listed under [Build options](#build-options). Prints the warnings and returns the dataset description. Throws when no security has prices, when the securities trade in different currencies (unless allowed), or when `:start` and `:end` leave no prices.

### `load-yahoo-dataset`

`(load-yahoo-dataset dir)`

Loads a built dataset: its prices as the main dataset and its fundamentals. Unloads a Compustat-style dataset. Returns a description of the date range and the fundamentals.

### `yahoo-dataset!`

`(yahoo-dataset! dir tickers & options)`

Downloads, builds and loads in one call. Takes the options of `download-yahoo!` and `build-yahoo-dataset!`, and prints the tickers that could not be downloaded.

```clojure
(yahoo-dataset! "data/banks" ["JPM" "BAC" "WFC" "C" "XLF"] :start "2018-01-01")
```

### `dataset-info`

`(dataset-info dir)`

The description `build-yahoo-dataset!` wrote to `<dir>/dataset.edn`: `:date-range`, `:securities` (per ticker: first and last date, number of days, currency, reporting currency, number of snapshots and a summary of its fundamentals), `:fx-pairs`, `:options` and `:warnings`.

### `default-fields`

The 54 Yahoo statement fields downloaded unless `:fields` says otherwise: the inputs of every [ratio](/posts/fundamentals/#ratios) and of most textbook factors. They include `TotalRevenue`, `GrossProfit`, `OperatingIncome`, `EBITDA`, `NetIncome`, `DilutedEPS`, `TotalAssets`, `CurrentAssets`, `CurrentLiabilities`, `TotalDebt`, `LongTermDebt`, `StockholdersEquity`, `CommonStockEquity`, `OrdinarySharesNumber`, `OperatingCashFlow`, `CapitalExpenditure`, `FreeCashFlow` and `CashDividendsPaid`. In the fundamentals they are spelled in kebab case: `:total-revenue`, `:diluted-eps`, and so on.

### Regenerating the sample

`lein run -m clojure-backtesting.sample-yahoo` rewrites the synthetic snapshots in `resources/sample-yahoo`. They are generated from fixed random seeds, so the result is the same every time.
