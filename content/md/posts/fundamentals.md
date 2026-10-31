{:title "Fundamentals"
 :date "2026-10-31"
 :layout :post
 :section "API Reference"
 :lead "Financial statement values as they were known on the backtest date, and the valuation, quality and growth ratios built on them."
 :toc true}

The functions on this page, in `clojure-backtesting.fundamentals`, answer one question: *what did the market know about this company on the backtest date?* They read the point-in-time fundamentals of a [Yahoo Finance dataset](/posts/yahoo-data/), loaded with `load-yahoo-dataset`. (A Compustat-style dataset works differently: its columns are joined onto the daily rows; see [Data and Trading Rules](/posts/user-guide/#crsp-and-compustat).)

The examples use the library's synthetic Yahoo sample with the clock at **2024-11-15**, after all three companies have reported their third quarter:

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
(init-portfolio "2024-11-15" 100000)
```

## How a value is chosen

Every lookup follows the same rules, so that a strategy cannot use information it would not have had:

1. **Only what was public.** A value becomes visible on the first trading day its report was public, never earlier. Report dates come from Yahoo, or from an assumed reporting lag when Yahoo has none ([Report dates](/posts/yahoo-data/#report-dates)). The [Key Concepts](/posts/concepts/#fundamentals-as-they-were-known) page shows this as a timeline.
2. **Per share of the day.** Per-share values (EPS) and share counts are stated per share of the backtest date, so they match the day's quoted price even when the company split its shares later.
3. **In the trading currency.** Values reported in another currency are converted at the previous day's exchange rate. The original stays available in `fundamental-info`.
4. **Not too old.** A value whose period ended more than `FUNDAMENTALS-MAX-AGE-MONTHS` (18) months before the day is ignored: the company has stopped reporting, or the data has no newer period.
5. **`nil` rather than a guess.** When a value cannot be stated this way (nothing public yet, too old, no exchange rate, or an unknown field), the result is `nil`. Ratios also return `nil` where a number would mislead, such as a P/E on losses.

## Fields and frequencies

Fields are Yahoo's statement fields in kebab case: `:total-revenue`, `:gross-profit`, `:operating-income`, `:ebitda`, `:net-income`, `:diluted-eps`, `:total-assets`, `:stockholders-equity`, `:total-debt`, `:ordinary-shares-number`, `:operating-cash-flow`, `:free-cash-flow`, and so on. [`available-fields`](#available-fields) lists what a security has; the [download](/posts/yahoo-data/#default-fields) decides which fields are there.

Each value belongs to a period, and you choose which kind:

| Frequency | Meaning |
| --------- | ------- |
| `:quarterly` | The latest quarter |
| `:annual` | The latest fiscal year |
| `:trailing` | Yahoo's own trailing twelve-month figure |
| `:ttm` | Twelve months, from whichever of these ends latest: Yahoo's trailing figure, the sum of the last four quarters, or the last fiscal year. Income-statement and cash-flow fields only; `nil` for balance-sheet fields |
| `:latest` | The latest quarter or fiscal year, whichever ends latest |

Without a frequency, income-statement and cash-flow fields use `:ttm`, and balance-sheet fields use `:latest`. That is usually what you want: a year of earnings, and the most recent balance sheet.

```clojure
(fundamental "ALFA" :net-income)              ;; => 2.3581160625000006E8   twelve months to 2024-09-30
(fundamental "ALFA" :net-income :quarterly)   ;; => 6.0394125000000015E7   the quarter to 2024-09-30
(fundamental "ALFA" :net-income :annual)      ;; => 2.1961500000000006E8   fiscal 2023
(fundamental "ALFA" :stockholders-equity)     ;; => 1.4349644100000005E9   balance sheet of 2024-09-30
(fundamental "ALFA" :diluted-eps)             ;; => 6.1839531575471245     twelve months, per share of today
```

### Where a twelve-month value comes from

`:ttm` takes the candidate with the latest period end; on a tie, Yahoo's trailing figure wins over the sum of four consecutive quarters, and that over the fiscal year. Over the sample, ALFA's twelve-month net income comes from all three sources in turn:

| Date | Source (`:source`) | Period end | Net income |
| ---- | ------------------ | ---------- | ---------- |
| 2022-01-03 | `:annual` | 2020-12-31 | $165.0M |
| 2022-03-01 | `:annual` | 2021-12-31 | $181.5M |
| 2023-11-01 | `:annual` | 2022-12-31 | $199.7M |
| 2024-03-01 | `:sum-of-quarters` | 2023-12-31 | $219.6M |
| 2024-08-01 | `:sum-of-quarters` | 2024-06-30 | $230.3M |
| 2024-11-15 | `:trailing` | 2024-09-30 | $235.8M |

On 2023-11-01 only three quarters of 2023 were public, so the fiscal year 2022 was still the latest full twelve months. Share counts and rates are not added up over quarters; for them, `:ttm` takes the latest quarter.

## Look up values

### `fundamental`

`(fundamental permno field [frequency])`

The latest public value of `field` for `permno` on the backtest date, or `nil`. See [Fields and frequencies](#fields-and-frequencies).

```clojure
(fundamental "ALFA" :total-revenue)   ;; => 1.5720773750000005E9
(fundamental "DLTA" :net-income)      ;; => nil, DLTA has no statements
(fundamental "ALFA" :no-such-field)   ;; => nil
```

### `fundamental-info`

`(fundamental-info permno field [frequency])`

Like `fundamental`, but returns the value together with where it comes from:

```clojure
(fundamental-info "ALFA" :net-income)
```

```text
{:period-end "2024-09-30",
 :reported-value 2.3581160625000006E8,
 :reported-currency "USD",
 :value 2.3581160625000006E8,
 :frequency :trailing,
 :field :net-income,
 :source :trailing,
 :currency "USD",
 :report-date-source :earnings-history,
 :public-date "2024-10-31",
 :report-date "2024-10-30"}
```

| Key | Meaning |
| --- | ------- |
| `:value` | The value, per share of today and in `:currency` |
| `:currency` | The trading currency, in its major unit (pounds, not pence) |
| `:reported-value`, `:reported-currency` | The value as reported |
| `:period-end` | The last day of the period the value covers |
| `:frequency` | The kind of period: `:quarterly`, `:annual`, `:trailing`, or `:ttm` for a sum of quarters |
| `:source` | For `:ttm` lookups, which candidate won: `:trailing`, `:sum-of-quarters` or `:annual`, or `:quarterly` for share counts and rates. For other lookups, the same as `:frequency` |
| `:report-date` | The day the report was announced |
| `:public-date` | The first trading day the value is visible: the report date, or the next trading day for an announcement after the close |
| `:report-date-source` | How the date was found: `:earnings-history`, `:announcement`, or `:lag` when it is an assumed reporting lag |

`:report-date-source :lag` is worth checking before trusting a result that depends on the timing of reports. BETA's recent report dates are estimated:

```clojure
(select-keys (fundamental-info "BETA" :net-income :quarterly) [:period-end :public-date :report-date-source])
;; => {:period-end "2024-09-30", :public-date "2024-11-14", :report-date-source :lag}
```

GAMA reports in euros; its value is converted at the previous day's rate, 1.18745 dollars per euro:

```clojure
(select-keys (fundamental-info "GAMA" :total-revenue) [:value :currency :reported-value :reported-currency])
;; => {:value 2.8602568584212475E9, :currency "USD", :reported-value 2.40873877504E9, :reported-currency "EUR"}
```

### `fundamental-as-of`

`(fundamental-as-of date permno field [frequency])`, and `(fundamental-info-as-of date permno field frequency)`

Like `fundamental` and `fundamental-info`, for another date than the backtest date. Useful for comparisons and for checking when a value appeared; a strategy should normally use the backtest date. ALFA reported its third quarter on 2024-10-30 after the close:

```clojure
(fundamental-as-of "2024-10-30" "ALFA" :diluted-eps :quarterly)   ;; => 1.508281257938323   second quarter
(fundamental-as-of "2024-10-31" "ALFA" :diluted-eps :quarterly)   ;; => 1.5876644820403398  third quarter
```

> **Watch out:** a date after the backtest date looks into the future. The function does not stop you.

### `fundamental-history`

`(fundamental-history permno field frequency n)`

Up to `n` values of `field` at `frequency` (`:quarterly`, `:annual` or `:trailing`) that were public on the backtest date, newest first, each described like `fundamental-info`. Unlike `fundamental`, it includes old values regardless of their age.

```clojure
(map #(select-keys % [:period-end :value :public-date])
     (fundamental-history "ALFA" :diluted-eps :quarterly 5))
```

```text
({:period-end "2024-09-30", :value 1.5876644820403398, :public-date "2024-10-31"}
 {:period-end "2024-06-30", :value 1.508281257938323, :public-date "2024-07-31"}
 {:period-end "2024-03-31", :value 1.5876644820403398, :public-date "2024-05-01"}
 {:period-end "2023-12-31", :value 1.5003429355281213, :public-date "2024-02-12"}
 {:period-end "2023-09-30", :value 1.428898033836306, :public-date "2023-10-31"})
```

All five are per share of 2024-11-15, after the 4-for-1 split. Asked on 2024-07-31, before the split, the same quarters come back per share of that day, four times larger: 6.033125031753292 for the quarter ending 2024-06-30.

### `available-fields`

`(available-fields permno)`

The fields known for a security, sorted. The sample has 18; a real download with the default fields has many more.

```clojure
(available-fields "ALFA")
;; => (:cash-and-cash-equivalents :common-stock-equity :current-assets :current-liabilities
;;     :diluted-average-shares :diluted-eps :ebitda :free-cash-flow :gross-profit :long-term-debt
;;     :net-income :operating-cash-flow :operating-income :ordinary-shares-number
;;     :stockholders-equity :total-assets :total-debt :total-revenue)
(available-fields "DLTA")
;; => ()
```

## Ratios

Each ratio is a function of one security, on the backtest date. They combine the day's price with the fundamentals public that day, and return `nil` when an input is missing or the ratio would be meaningless. Being plain functions, they plug straight into [`rank-by` and `top-n`](/posts/strategy/#ranking-and-screening).

| Function | Definition | ALFA | BETA | GAMA | DLTA |
| -------- | ---------- | ---: | ---: | ---: | ---: |
| `market-cap` | Price × shares outstanding | $4.34B | $2.08B | $12.43B | `nil` |
| `enterprise-value` | Market cap + total debt − cash | $4.82B | $2.35B | $13.29B | `nil` |
| `pe-ratio` | Price / twelve-month diluted EPS; `nil` unless EPS is positive | 18.63 | 30.01 | 36.59 | `nil` |
| `earnings-yield` | Twelve-month diluted EPS / price; negative on losses | 5.4% | 3.3% | 2.7% | `nil` |
| `book-to-market` | Book equity / market cap | 0.33 | 0.39 | 0.21 | `nil` |
| `price-to-book` | Market cap / book equity; `nil` unless equity is positive | 3.02 | 2.54 | 4.83 | `nil` |
| `sales-to-price` | Twelve-month revenue / market cap | 0.36 | 0.42 | 0.23 | `nil` |
| `free-cash-flow-yield` | Twelve-month free cash flow / market cap | 4.9% | 3.0% | 2.5% | `nil` |
| `ev-to-ebitda` | Enterprise value / twelve-month EBITDA; `nil` unless EBITDA is positive | 12.26 | 27.06 | 19.36 | `nil` |
| `dividend-yield` | Dividends that went ex in the last 365 days / price | 0.35% | 0.00% | 0.00% | 0.00% |
| `return-on-equity` | Twelve-month net income / book equity | 16.4% | 8.5% | 13.3% | `nil` |
| `return-on-assets` | Twelve-month net income / total assets | 7.4% | 3.8% | 6.0% | `nil` |
| `gross-margin` | Twelve-month gross profit / revenue | 45.0% | 30.0% | 55.0% | `nil` |
| `operating-margin` | Twelve-month operating income / revenue | 20.0% | 5.0% | 18.0% | `nil` |
| `net-margin` | Twelve-month net income / revenue | 15.0% | 8.0% | 12.0% | `nil` |
| `debt-to-equity` | Total debt / book equity | 0.56 | 0.56 | 0.56 | `nil` |
| `current-ratio` | Current assets / current liabilities | 2.00 | 2.00 | 2.00 | `nil` |
| `piotroski-f-score` | See [below](#piotroski-f-score) | 4 | 4 | 7 | `nil` |

The table rounds the values; the functions return plain numbers, such as `(pe-ratio "ALFA")` ⇒ `18.632838423003843`, a price of $115.22 over a twelve-month EPS of $6.18.

Some notes on the inputs:

- *Book equity* is common stock equity, else stockholders' equity. *Cash* is cash and equivalents, else cash plus short-term investments.
- `dividend-yield` uses the dividends in the price history, so it works without statements (DLTA's is 0, not `nil`).
- Besides the ratios marked "`nil` unless positive", `return-on-equity`, `return-on-assets`, the margins, `debt-to-equity` and `current-ratio` are also `nil` when their denominator is zero or negative: a company with negative equity has no meaningful return on equity. The other ratios are `nil` only for a zero denominator.
- The sample's margins are identical every year by construction. Real data is not so tidy.

### `shares-outstanding`

`(shares-outstanding permno)`

Shares outstanding in today's share basis: the latest ordinary shares number, else shares issued, else diluted average shares. It also checks the count against net income divided by diluted EPS. If the two differ more than twofold, which happens for a few companies with several share classes, it prints a warning once and returns `nil`, so `market-cap` and every ratio on market value are `nil` too rather than wrong.

```clojure
(shares-outstanding "ALFA")   ;; => 3.765920597604E7
```

### `market-cap`

`(market-cap permno)`

Today's price times `shares-outstanding`, in the trading currency's major unit.

### `growth`

`(growth permno field [frequency])`

Year-on-year growth of `field`: the latest value over the one a year earlier, minus 1. With `:annual` (the default) it compares the last two fiscal years; with `:quarterly`, the latest quarter with the same quarter a year before. `nil` when the year-earlier value is missing or not positive.

```clojure
(growth "ALFA" :total-revenue)              ;; => 0.09999999999999987    10% a year
(growth "GAMA" :net-income :quarterly)      ;; => 0.040000000000000036   4% on the same quarter of 2023
```

### Piotroski F-score

`(piotroski-signals permno)` and `(piotroski-f-score permno)`

Joseph Piotroski's F-score (2000) rates a company's financial health from 0 to 9, one point for each of nine signals computed from the last two fiscal years:

| Signal | True when |
| ------ | --------- |
| `:roa` | Net income over beginning-of-year assets is positive |
| `:cfo` | Operating cash flow is positive |
| `:delta-roa` | Return on assets improved on the year before |
| `:accrual` | Operating cash flow exceeds net income |
| `:delta-leverage` | Long-term debt over assets fell |
| `:delta-liquidity` | The current ratio rose |
| `:no-dilution` | The number of shares did not increase |
| `:delta-margin` | The gross margin rose |
| `:delta-turnover` | Revenue over beginning-of-year assets rose |

`piotroski-signals` returns each signal as `true`, `false`, or `nil` when an input is missing. `piotroski-f-score` counts the true ones, and is `nil` unless all nine can be computed, which takes three fiscal years of total assets.

```clojure
(piotroski-signals "ALFA")
;; => {:delta-liquidity false, :no-dilution true, :accrual true, :delta-margin false, :delta-leverage false,
;;     :cfo true, :delta-turnover false, :delta-roa false, :roa true}
(piotroski-f-score "ALFA")
;; => 4
```

A high score (8 or 9) marks a company whose finances are improving on most fronts; a low one (0 to 2), the opposite. Piotroski found that among cheap stocks (high book-to-market), the high scorers did much better than the low ones. The [Quality and Earnings](/posts/examples/example-quality-and-earnings/) example uses it.

> **Watch out:** a signal that compares two exactly equal ratios is decided by rounding. In the sample, GAMA's margins and turnover are identical every year and its values are converted from euros, so three of its signals flip with the exchange rate: its score is 5 on 2024-06-03, 6 on 2024-08-01 and 7 on 2024-11-15. Real accounts are almost never exactly equal from one year to the next.

## Earnings announcements

### `latest-earnings`

`(latest-earnings permno)`

The latest earnings announcement public on the backtest date, or `nil`:

```clojure
(latest-earnings "ALFA")
;; => {:report-date "2024-10-30", :public-date "2024-10-31", :period-end "2024-09-30",
;;     :eps-actual 1.5877, :eps-estimate 1.54, :surprise-pct 3.09}
```

`:surprise-pct` is how far the actual EPS beat (positive) or missed (negative) the analysts' estimate, in percent. Use it rather than the EPS figures: Yahoo's EPS here is the analysts' *adjusted* figure, and its share basis is not consistent across splits. `:period-end` is `nil` when Yahoo only has the announcement date.

### `days-since-earnings`

`(days-since-earnings permno)`

Calendar days since the latest public earnings announcement, or `nil`. On 2024-11-15, 15 days after ALFA's report became public:

```clojure
(days-since-earnings "ALFA")   ;; => 15
```

Together, the two make event strategies short: buy on the day of a positive surprise, `(and (= (:public-date e) (get-date)) (pos? (or (:surprise-pct e) 0)))`, and hold for a few weeks. The [Quality and Earnings](/posts/examples/example-quality-and-earnings/#buying-after-good-news) example does that.

## Loading and settings

`load-yahoo-dataset` loads the fundamentals for you. These functions are for other cases.

### `load-fundamentals` and `clear-fundamentals!`

`(load-fundamentals path)` loads a `fundamentals.edn` file as the build writes it, replacing any fundamentals loaded before; useful to pair one dataset's prices with fundamentals built with other options. `(clear-fundamentals!)` unloads them.

```clojure
(load-fundamentals "target/sample-yahoo/fundamentals.edn")
;; => "Fundamentals: 5 securities, 33 reports, 1 FX series"
```

### `fx-rate`

`(fx-rate from to date)`

Units of `to` per unit of `from` known on `date`: the latest rate dated *before* it. `1.0` for the same currency, `nil` when unknown.

```clojure
(fx-rate "EUR" "USD" "2024-11-15")   ;; => 1.18745
(fx-rate "GBP" "USD" "2024-11-15")   ;; => nil, not in the sample
```

### `FUNDAMENTALS-MAX-AGE-MONTHS`

How many months after its period end a value stays usable, 18 by default. Change it with `update-fundamentals-max-age` (see [Parameters](/posts/parameters/#fundamentals-max-age-months)). On 2022-01-03 ALFA's latest public year is 2020, which ended 12 months and 3 days earlier:

```clojure
(init-portfolio "2022-01-03" 100000)
(fundamental "ALFA" :net-income)   ;; => 1.65E8, fiscal 2020
(update-fundamentals-max-age 12)
(fundamental "ALFA" :net-income)   ;; => nil, too old
(update-fundamentals-max-age 18)   ; back to the default
```
