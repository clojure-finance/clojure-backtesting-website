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

This page introduces the API related to calculating stock market indicators. All the functions here without extra specified, will calculate indicator for the current date, according to the global date pointer.

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
=> (moving-avg "28636" 10)
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
=> (moving-sd "28636" 10)
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
=> (EMA "28636")
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
=> (EMA10 "28636")
8.0375
=> (def EMA20 (EMA-generator 20))
=> (= (EMA20 "28636") (EMA "28636"))
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
=> (MACD "28636")
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
=> (MACD102030 "28636")
[-0.03958333333333286 8.0375 8.70625 8.745833333333334]
=> (def MACD91226 (MACD-generator 9 12 26))
=> (= (MACD91226 "28636") (MACD "28636"))
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
(println (ROC "58043" 20)
```

<br>

### `RSI`

This function calculates the Relative Strength Index (RSI) indicator.

**Parameters:**

- `permno` - the name of the security

**Output:**

- `RSI`

**Related Parameter:**

- `RSI-CYCLE`: By default 14.


**Possible usages:**

```clojure
(RSI "58043" 20)
```

<br>

### `RSI-generator`

This function generates user-defined Relative Strength Index (RSI) calculator.

**Parameters:**

- `n` - time window

**Output:**

- `RSI`


**Possible usages:**

```clojure
=> (def RSI10 (RSI-generator 10))
=> (RSI10 "28636")
52.67
=> (def RSI14 (RSI-generator 14))
=> (= (RSI14 "28636") (RSI "28636"))
true
```

<br>

## Volatility indicators

### `ATR`

This function calculates standard deviation of a stock for the last n days.

**Parameters:**

- `permno` - the name of the security
- `n` - average of the daily TR values for the last n days

**Possible usages:**

```clojure
=> (ATR "28637" 10)
```

<br>


### `keltner-channel`

This function calculates the Keltner Channel value of a stock for the last n days.

**Parameters:**

- `permno` - the name of the security
- `window` - time window

**Possible usages:**

```clojure
=> (keltner-channel "28637" 10)
```

<br>