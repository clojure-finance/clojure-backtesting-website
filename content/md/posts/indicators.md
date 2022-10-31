{:title "Indicators"
 :date "2021-01-18"
 :layout :post
 :tags  []
 :toc true}

# Indicators

##### This page introduces the API related to calculating stock market indicators. All the functions here without extra specified, will calculate indicator for the current date, according to the global date pointer.

## Trend Indicators

#### moving-avg

Calculate the n days' moving average of a security.

| Argument | Type                 | Function                                             | Remarks |
| -------- | -------------------- | ---------------------------------------------------- | ------- |
| `permno` | String               | Indicate the target security                         |         |
| `n`      | Positive Integer > 1 | Indicate length of the moving window including today |         |

**Return**

Double, if the security exists. `nil` otherwise.

**Example**

```clojure
=> (moving-avg "APL" 10)
```

#### moving-sd

Calculate the n days' moving standard deviation of a security.

| Argument | Type                 | Function                                             | Remarks |
| -------- | -------------------- | ---------------------------------------------------- | ------- |
| `permno` | String               | Indicate the target security                         |         |
| `n`      | Positive Integer > 1 | Indicate length of the moving window including today |         |

**Return**

Double, if the security exists. `nil` otherwise.

**Example**

```clojure
=> (moving-sd "APL" 10)
```

#### EMA

Calculate the EMA of a security. If called the first time, will return the `EMA-CYCLE`-day moving average. After this, EMA will be updated based on its [definition](https://corporatefinanceinstitute.com/resources/knowledge/trading-investing/exponential-moving-average-ema/). *This update process is automatic.*

| Argument | Type   | Function                     | Remarks |
| -------- | ------ | ---------------------------- | ------- |
| `permno` | String | Indicate the target security |         |

**Relevant Parameter**

`EMA-CYCLE`: the length of moving average when EMA first called.

**Return**

Double, if the security exists. `nil` otherwise.

**Example**

```clojure
=> (EMA "APL")
```

<br>

#### EMA-generator

Generator of the EMA function for the specified cycle. 

| Argument | Type         | Function                   | Remarks |
| -------- | ------------ | -------------------------- | ------- |
| `cycle`  | Positive Int | Indicate the size of cycle |         |

**Return**

EMA function having the same usage as above with a different cycle.

**Example**

```clojure
=> (def EMA10 (EMA-generator 10))
=> (EMA10 "APL")
8.0375
=> (def EMA20 (EMA-generator 20))
=> (= (EMA20 "APL") (EMA "APL"))
true
```

<br>

#### MACD

Calculate the [Moving average convergence / divergence ](https://www.investopedia.com/terms/m/macd.asp) of a security.

| Argument | Type   | Function                     | Remarks |
| -------- | ------ | ---------------------------- | ------- |
| `permno` | String | Indicate the target security |         |

**Relevant Parameter**

`MACD-SIGNAL`:  the cycle of the signal EMA
`MACD-SHORT`: the cycle of the short EMA
`MACD-LONG`: the cycle of the long EMA

**Return**

A sequence, [{MACD} {Signal EMA} {Short EMA} {Long EMA}], if the security exists. `nil` otherwise.

**Example**

```clojure
=> (MACD "APL")
[-0.34999999999999787 11.182222222222222 11.185 11.534999999999998]
```

<br>

#### MACD-generator

Generator of the MACD function for the specified cycle. 

| Argument       | Type         | Function                                          | Remarks |
| -------------- | ------------ | ------------------------------------------------- | ------- |
| `signal-cycle` | Positive Int | Indicate the size of EMA cycle of the signal line |         |
| `short-cycle`  | Positive Int | Indicate the size of EMA cycle of the short line  |         |
| `long-cycle`   | Positive Int | Indicate the size of EMA cycle of the long line   |         |

**Return**

MACD function having the same usage as above with a different cycle.

**Example**

```clojure
=> (def MACD102030 (MACD-generator 10 20 30))
=> (MACD102030 "APL")
[-0.03958333333333286 8.0375 8.70625 8.745833333333334]
=> (def MACD91226 (MACD-generator 9 12 26))
=> (= (MACD91226 "APL") (MACD "APL"))
true
```

<br>

### `parabolic-SAR`

This function calculates the Parabolic Stop and Reverse (SAR).

**Parameters:**

- `permno` - the symbol of a security
- `mode` - "lazy" or "non-lazy
- `af` - acceleration factor
- `prev-psar` - SAR value of the previous period

**Output:**

- `parabolic-SAR`

**Possible usages:**

```clojure
(let [prev-close (Double/parseDouble (get (first (get-prev-n-days :PRC 1 "OMFGA")) :PRC))]
    (println (parabolic-SAR "OMFGA" "non-lazy" 0.2 prev-close))
)
```

## Momentum Indicators

### `ROC`

This function calculates the Rate of Change (ROC) indicator.

**Parameters:**

- `permno` - the name of the security
- `n` - time window

**Output:**

- `ROC`

**Possible usages:**

```clojure
(println (ROC "AAPL" 20)
```

<br>

### `RSI`

This function calculates the Relative Strength Index (RSI) indicator.

**Parameters:**

- `permno` - the name of the security
- `n` - time window

**Output:**

- `RSI`


**Possible usages:**

```clojure
(RSI "AAPL" 20)
```

<br>

## Volatility indicators

### `ATR`

This function calculates standard deviation of a stock for the last n days.

**Parameters:**

- `permno` - the name of the security
- `mode` - "lazy" or "non-lazy"
- `prev-atr` - previous ATR value
- `n` - average of the daily TR values for the last n days

**Possible usages:**

```clojure
(let [prev-atr (Double/parseDouble (get (first (get-prev-n-days :PRC 1 "OMFGA")) :PRC))]
    (println (ATR "OMFGA" "non-lazy" prev-atr 10))
    )
```

<br>


### `keltner-channel`

This function calculates the Keltner Channel value of a stock for the last n days.

**Parameters:**

- `permno` - the name of the security
- `mode` - "lazy" or "non-lazy"
- `window` - time window
- `prev-atr` - previous ATR value

**Possible usages:**

```clojure
(let [low-price (Double/parseDouble (get-by-key "OMFGA" :BIDLO "non-lazy"))
      high-price (Double/parseDouble (get-by-key "OMFGA" :ASKHI "non-lazy"))
      prev-atr (- high-price low-price)]
  (println (keltner-channel "OMFGA" "non-lazy" 10 prev-atr))
  )
```

<br>