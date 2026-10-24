{:title "Indicators"
 :date "2021-01-18"
 :layout :post
 :tags  []

:navbar? true
:page-index 6

 :toc true}

<style>
/* table styles */
table, th, td {
  border: 1px solid black;
  padding: 5px;
}
td {
  padding: 10px;
}
</style>

This page introduces the functions that calculate technical indicators. Unless stated otherwise, they calculate the indicator for the current date of the global date pointer, from the trading price (the closing price by default).

The examples ran on the bundled sample dataset with the date pointer at 1990-02-19, except the second `parabolic-SAR` call and the `ATR` and `keltner-channel` examples, which ran on 1990-02-20.

## Trend Indicators

#### moving-avg

Calculate the simple moving average of a security over the last n trading days, including today.

| Argument | Type                 | Function                                             | Remarks |
| -------- | -------------------- | ---------------------------------------------------- | ------- |
| `permno` | String               | The target security                                  |         |
| `n`      | Positive Integer > 1 | Length of the moving window including today          |         |

**Return**

A number, or `nil` unless the security has a price on all n days.

**Example**

```clojure
=> (moving-avg "10001" 10)
48.217
```

<br>

#### moving-sd

Calculate the moving (sample) standard deviation of a security's price over the last n trading days, including today. Together with `moving-avg` it gives Bollinger Bands; see the [`bollinger_bands.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/bollinger_bands.clj) worksheet.

| Argument | Type                 | Function                                             | Remarks |
| -------- | -------------------- | ---------------------------------------------------- | ------- |
| `permno` | String               | The target security                                  |         |
| `n`      | Positive Integer > 1 | Length of the moving window including today          |         |

**Return**

A number, or `nil` unless the security has a price on all n days.

**Example**

```clojure
=> (moving-sd "10001" 10)
0.5631666420045367
```

<br>

#### EMA

Calculate the exponential moving average of a security. The first call returns the `EMA-CYCLE`-day moving average. After that, the backtester updates the EMA every trading day according to its [definition](https://corporatefinanceinstitute.com/resources/knowledge/trading-investing/exponential-moving-average-ema/), as long as the run continues. *This update is automatic.*

| Argument | Type   | Function                     | Remarks |
| -------- | ------ | ---------------------------- | ------- |
| `permno` | String | The target security          |         |

**Relevant Parameter**

`EMA-CYCLE`: the cycle of the EMA (20 by default).

**Return**

A number, or `nil` if the first moving average cannot be computed yet.

**Example**

```clojure
=> (EMA "10001")
48.4835
```

<br>

#### EMA-generator

Create an EMA function for another cycle.

| Argument | Type         | Function                   | Remarks |
| -------- | ------------ | -------------------------- | ------- |
| `cycle`  | Positive Int | The cycle of the EMA       |         |

**Return**

An EMA function with the same usage as `EMA`, using the given cycle.

**Example**

```clojure
=> (def EMA10 (EMA-generator 10))
=> (EMA10 "10001")
48.217
=> (def EMA20 (EMA-generator 20))
=> (= (EMA20 "10001") (EMA "10001"))
true
```

<br>

#### MACD

Calculate the [moving average convergence / divergence](https://www.investopedia.com/terms/m/macd.asp) of a security. Like `EMA`, it is updated automatically every trading day after the first call.

| Argument | Type   | Function                     | Remarks |
| -------- | ------ | ---------------------------- | ------- |
| `permno` | String | The target security          |         |

**Relevant Parameters**

`MACD-SIGNAL`: the cycle of the signal EMA (9 by default)

`MACD-SHORT`: the cycle of the short EMA (12 by default)

`MACD-LONG`: the cycle of the long EMA (26 by default)

**Return**

A vector [MACD, signal EMA, short EMA, long EMA], where MACD = short EMA − long EMA and each EMA is of the security's price. `nil` until there are enough prices for the long EMA.

**Example**

```clojure
=> (MACD "10001")
[-0.9610897435897385 48.14000000000001 48.16083333333333 49.12192307692307]
```

<br>

#### MACD-generator

Create a MACD function for other cycles.

| Argument       | Type         | Function                                          | Remarks |
| -------------- | ------------ | ------------------------------------------------- | ------- |
| `signal-cycle` | Positive Int | The EMA cycle of the signal line                  |         |
| `short-cycle`  | Positive Int | The EMA cycle of the short line                   |         |
| `long-cycle`   | Positive Int | The EMA cycle of the long line                    |         |

**Return**

A MACD function with the same usage as `MACD`, using the given cycles.

**Example**

```clojure
=> (def MACD51020 (MACD-generator 5 10 20))
=> (MACD51020 "10001")
[-0.2665000000000006 48.30400000000001 48.217 48.4835]
```

<br>

#### parabolic-SAR

Calculate Wilder's parabolic stop and reverse (SAR), one trading day per call. Pass the result of the previous day's call back in on the next trading day.

Needs the daily low and high columns `:BIDLO` and `:ASKHI` in the dataset.

| Argument | Type       | Function                                  | Remarks |
| -------- | ---------- | ----------------------------------------- | ------- |
| `permno` | String     | The target security                       |         |
| `state`  | Map / `nil` | The map returned by the previous day's call | `nil` on the first day: the trend is then read from today's price against the previous one. |

**Relevant Parameters**

`SAR-AF-STEP`: the starting acceleration factor and its increment (0.02 by default)

`SAR-AF-MAX`: the cap of the acceleration factor (0.2 by default)

**Return**

A map `{:sar :trend :ep :af}`: the stop level, the trend (`:up` or `:down`), the extreme point of the current trend, and the acceleration factor, which grows by `SAR-AF-STEP` on every new extreme.

**Example**

```clojure
=> (def sar (parabolic-SAR "10001" nil))
=> sar
{:sar 49.79, :trend :down, :ep 47.32, :af 0.02}

=> (next-date)
=> (parabolic-SAR "10001" sar)
{:sar 49.7406, :trend :down, :ep 47.2, :af 0.04}
```

<br>

## Momentum Indicators

#### ROC

Calculate the rate of change of a security's price over the last n trading days.

| Argument | Type             | Function             | Remarks |
| -------- | ---------------- | -------------------- | ------- |
| `permno` | String           | The target security  |         |
| `n`      | Positive Integer | The time window      |         |

**Return**

The rate of change as a decimal (today's price / the price n trading days ago − 1), or `nil` if there are fewer than n earlier days or either price is missing.

**Example**

```clojure
=> (ROC "10001" 10)
-0.014546965918536903
```

<br>

#### RSI

Calculate the Relative Strength Index of a security over `RSI-CYCLE` days. The first call seeds it with the simple average gain and loss; after that it is updated every trading day with Wilder's smoothing. *This update is automatic.*

| Argument | Type   | Function             | Remarks |
| -------- | ------ | -------------------- | ------- |
| `permno` | String | The target security  |         |

**Relevant Parameter**

`RSI-CYCLE`: 14 by default.

**Return**

A number between 0 and 100, or `nil` unless the security has a price on all of the first `RSI-CYCLE` days.

**Example**

```clojure
=> (RSI "10001")
45.56962025316458
```

<br>

#### RSI-generator

Create an RSI function for another cycle.

| Argument | Type         | Function          | Remarks |
| -------- | ------------ | ----------------- | ------- |
| `cycle`  | Positive Int | The time window   |         |

**Return**

An RSI function with the same usage as `RSI`, using the given cycle.

**Example**

```clojure
=> (def RSI10 (RSI-generator 10))
=> (RSI10 "10001")
36.27992633517498
=> (def RSI14 (RSI-generator 14))
=> (= (RSI14 "10001") (RSI "10001"))
true
```

<br>

## Volatility Indicators

#### ATR

Calculate the average true range of a security, Wilder-smoothed from the previous value: (previous ATR × (n − 1) + today's true range) / n. The true range is the largest of high − low, |high − previous close| and |low − previous close|.

Needs the daily low and high columns `:BIDLO` and `:ASKHI` in the dataset.

| Argument   | Type             | Function                     | Remarks |
| ---------- | ---------------- | ---------------------------- | ------- |
| `permno`   | String           | The target security          |         |
| `n`        | Positive Integer | The smoothing window         |         |
| `prev-atr` | Number           | The previous day's ATR       | On the first day, seed it with, e.g., today's high − low. |

**Return**

A number.

**Example**

```clojure
=> (ATR "10001" 14 0.5)
0.5114285714285712

;; carry the ATR from day to day
(def atr (atom (- (get-permno-by-key "10001" :ASKHI)
                  (get-permno-by-key "10001" :BIDLO))))
;; then, on each trading day:
(swap! atr #(ATR "10001" 14 %))
```

<br>

#### keltner-channel

Calculate the Keltner Channel of a security: the `EMA` as the middle line, with bands two ATRs above and below it.

| Argument   | Type             | Function                        | Remarks |
| ---------- | ---------------- | ------------------------------- | ------- |
| `permno`   | String           | The target security             |         |
| `window`   | Positive Integer | The ATR smoothing window        |         |
| `prev-atr` | Number           | The previous day's ATR          | As for `ATR` |

**Return**

A vector [middle upper lower].

**Example**

```clojure
=> (keltner-channel "10001" 14 0.5114285714285712)
[48.38030952380952 49.42439115646258 47.33622789115646]
```
