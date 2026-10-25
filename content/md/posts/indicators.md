{:title "Indicators"
 :date "2021-01-18"
 :layout :post
 :section "API Reference"
 :lead "Trend, momentum and volatility indicators, computed from the trading price up to today."
 :toc true}

This page introduces the functions that calculate technical indicators. Unless stated otherwise, they calculate the indicator for the current date of the global date pointer, from the trading price (the closing price by default).

The examples ran on the bundled sample dataset with the date pointer at 1990-02-19, except the second `parabolic-SAR` call and the `ATR` and `keltner-channel` examples, which ran on 1990-02-20, and the `MACD` examples, which show their own steps.

## Which Indicator Does What

| Indicator | Kind | What it tells you | Typical use |
| --------- | ---- | ----------------- | ----------- |
| [`moving-avg`](#moving-avg) | Trend | The average price over the last n days | Crossovers such as the [Golden Cross](/posts/examples/example-golden-cross/) |
| [`moving-sd`](#moving-sd) | Volatility | How much the price varied over the last n days | [Bollinger Bands](/posts/examples/example-bollinger-bands/) |
| [`EMA`](#ema) | Trend | An average that weights recent days more | Faster trend signals |
| [`MACD`](#macd) | Trend | The gap between a short and a long EMA, and its signal line | Buy when MACD crosses above its signal line |
| [`parabolic-SAR`](#parabolic-sar) | Trend | A stop level that trails the price and flips with the trend | Trailing stops |
| [`ROC`](#roc) | Momentum | The percentage change over n days | Buy strength, sell weakness |
| [`RSI`](#rsi) | Momentum | Recent gains against recent losses, from 0 to 100 | Below 30 oversold, above 70 overbought |
| [`ATR`](#atr) | Volatility | The average daily trading range | Sizing positions and stops |
| [`keltner-channel`](#keltner-channel) | Volatility | Bands two ATRs around the EMA | Breakouts |

> **Good to know:** every indicator returns `nil` until there is enough history, and works on the raw trading price, so a stock split looks like a sudden drop. `EMA`, `MACD` and `RSI` keep their own state and update it every trading day once you have called them; `parabolic-SAR` and `ATR` instead take yesterday's value as an argument.

## Trend Indicators

### `moving-avg`

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

### `moving-sd`

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

### `EMA`

Calculate the exponential moving average of a security. The first call returns the `EMA-CYCLE`-day moving average. After that, the backtester updates the EMA every trading day according to its [definition](https://corporatefinanceinstitute.com/resources/knowledge/trading-investing/exponential-moving-average-ema/), as long as the run continues. *This update is automatic.* On a day without a price, it keeps its last value.

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

### `EMA-generator`

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

### `MACD`

Calculate the [moving average convergence / divergence](https://www.investopedia.com/terms/m/macd.asp) of a security. After the first call, it is updated automatically every trading day, even while it still returns `nil`.

| Argument | Type   | Function                     | Remarks |
| -------- | ------ | ---------------------------- | ------- |
| `permno` | String | The target security          |         |

**Relevant Parameters**

`MACD-SIGNAL`: the cycle of the signal line (9 by default)

`MACD-SHORT`: the cycle of the short EMA (12 by default)

`MACD-LONG`: the cycle of the long EMA (26 by default)

**Return**

A vector [MACD, signal, short EMA, long EMA]. The short and long EMAs are of the security's price, MACD = short EMA − long EMA, and the signal line is the `MACD-SIGNAL`-day EMA of MACD, seeded with the mean of its first `MACD-SIGNAL` values.

`nil` until the signal line has been seeded. With the default cycles, that takes 26 closes for the long EMA and then 9 MACD values from the days it has been tracked; so call `MACD` once early in the run, as below, to get the first value on the 34th close.

**Example**

```clojure
=> (init-portfolio "1990-01-02" 10000)
=> (MACD "10001") ;; not enough prices yet, but now tracked every day
nil
=> (while (< (compare (get-date) "1990-02-28") 0) (next-date))
=> (MACD "10001")
[-0.4655478988484276 -0.6167045545421674 48.08274496033748 48.548292859185906]
```

### `MACD-generator`

Create a MACD function for other cycles.

| Argument       | Type         | Function                                          | Remarks |
| -------------- | ------------ | ------------------------------------------------- | ------- |
| `signal-cycle` | Positive Int | The EMA cycle of the signal line                  |         |
| `short-cycle`  | Positive Int | The EMA cycle of the short line                   |         |
| `long-cycle`   | Positive Int | The EMA cycle of the long line                    |         |

**Return**

A MACD function with the same usage as `MACD`, using the given cycles. Generators with the same cycles return the same function.

**Example**

```clojure
=> (init-portfolio "1990-01-02" 10000)
=> (def MACD51020 (MACD-generator 5 10 20))
=> (MACD51020 "10001")
nil
=> (while (< (compare (get-date) "1990-02-28") 0) (next-date))
=> (MACD51020 "10001")
[-0.23347193957126677 -0.31630965090941965 48.056297547397676 48.28976948696894]
```

### `parabolic-SAR`

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

## Momentum Indicators

### `ROC`

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

### `RSI`

Calculate the Relative Strength Index of a security over `RSI-CYCLE` days. The first call seeds it with the simple average gain and loss; after that it is updated every trading day with Wilder's smoothing. *This update is automatic.* Through days without a price it keeps its last value, and the change across the gap counts as one day's change.

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

### `RSI-generator`

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

## Volatility Indicators

### `ATR`

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

### `keltner-channel`

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
