{:title "Indicators"
 :layout :post
 :tags  []
 :toc true}

<br>

**`ns: indicators`**

This namespace encapsulates some popular indicators in stock market analysis.

---

## EMA

Exponential Moving Average

<br>

### `EMA`

Note that the result is true after you call it `EMA-CYCLE` in a row.

(Assocated parameter: `EMA-CYCLE`, by default, 20)

**Parameters:**

- `price` - the price of a ticker
- `prev-ema` - ema of the last day

**Possible usages:**

```clojure
(def prev-EMA (get-price "AAPL")) ;;assign the lastest price to prev EMA as initialization
(while
  (def prev-EMA (EMA (get-price "AAPL") prev-EMA))) ;;continuously overwrite prev-EMA with the latest result
```

<br>

### `tic-EMA`

An alternative wrapper function of the `EMA` function.

**Parameters:**

- `tic` - ticker name
- `key` - key of the price
- `prev-EMA` - ema of the last day

**Possible usages:**

```clojure
(def prev-EMA (get-price "AAPL")) ;;assign the lastest price to prev EMA as initialization
(while
  (def prev-EMA (tic-EMA "AAPL" :PRC prev-EMA))) ;;continuously overwrite prev-EMA with the latest result
```

<br>

## MACD

Moving Average Convergence / Divergence

### `MACD`

**Parameters:**

- `price` - the price of a ticker
- `ema-12` - 12-day ema of the last day
- `ema-26` - 26-day ema of the last day

**Output:**

[MACD new-ema-12 new-ema-26]

**Possible usages:**

```clojure
(def ema-12 (get-price "AAPL")) 
(def ema-26 (get-price "AAPL"))
;;assign the lastest price to prev EMA as initialization
(while
  (let [tmp (MACD (get-price "AAPL") ema-12 ema-26)
        new-MACD (first tmp)] ;; this is the MACD result
    (def ema-12 (nth tmp 1))
    (def ema-26 (nth tmp 2)))) ;;continuously overwrite prev-EMA with the latest result
```

<br>