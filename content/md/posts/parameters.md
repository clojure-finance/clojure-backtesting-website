{:title "Configurable Parameters"
 :date "2021-01-23"
 :layout :post
 :tags  []
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
<br>



This namespace defines the parameters that are configurable in the backtester.

---

## Trading Price

The trading price is by default set to be closing price.

You can change it to opening price by changing the parameter.clj in `src/`:

```clojure
(def PRICE-KEY :OPRNPRC)
```

-----------------

## Margin Requirements

<br>

### `INITIAL-MARGIN`

This variable stores the minimum ratio of cash to total value of securities that must be paid when buying on margin. The default value is set as `0.5`.

**Example:**

```clojure
;; check the variable
(println INITIAL-MARGIN)

;; output:
0.5
```

<br>

### `update-initial-margin`

This function updates the initial margin.

**Parameters:**

- `num` - new initial margin value, needs to be > 0

**Example:**

```clojure
;; update the variable
(update-initial-margin 0.7)
(println INITIAL-MARGIN)

;; output:
0.7
```

<br>

### `MAINTENANCE-MARGIN`

This variable stores the minimum ratio of cash to total value of securities that must be maintained in the margin account. If the portfolio margin goes below the maintenance margin, all positions will be automatically closed. The default value is set as `0.25`.

**Example:**

```clojure
;; check the variable
(println MAINTENANCE-MARGIN)

;; output:
0.25
```

<br>

### `update-maintenance-margin`

This function updates the maintenance margin.

**Parameters:**

- `num` - new maintenance margin value, needs to be > 0

**Example:**

```clojure
;; update the variable
(update-maintenance-margin 0.2)
(println MAINTENANCE-MARGIN)

;; output:
0.2
```

---

## Transaction Cost & Loan Interest

<br>

### `TRANSACTION-COST`

This variable stores the rate of the commission fee that is charged upon the execution of an order. The default value is `0.0`.

**Example:**

```clojure
;; check the variable
(println TRANSACTION-COST)

;; output:
0.0
```


<br>

### `update-transaction-cost`

This function updates the transaction cost.

**Parameters:**

- `num` - new initial margin value, needs to be within the range of [0,1).

**Example:**

```clojure
;; update the variable
(update-transaction-cost 0.2)
(println TRANSACTION-COST)

;; output:
0.2
```

<br>


### `INTEREST-RATE`

This variable stores the simple interest rate (per annum) that is incurred when making a loan. Loan interests will be deducted at the end of every month. The default value is `0.0`.

**Example:**

```clojure
;; check the variable
(println INTEREST-RATE)

;; output:
0.0
```

<br>

### `update-interest-rate`

This function updates the interest rate.

**Parameters:**

- `num` - new initial margin value, needs to be within the range of [0,1).


```clojure
;; update the variable
(update-interest-rate 0.3)
(println INTEREST-RATE)

;; output:
0.3
```

<br>

## Indicator Parameters

Below parameters are associated with corresponding indicator calculators. They can all be changed by function `CHANGE-{parameter name}`. For example, `EMA-CYCLE` can be changed by `(CHANGE-EMA-CYCLE 15)`.

### `EMA-CYCLE`

Cycle of the EMA formula. By default, 20.

### `MACD-SIGNAL` 

Cycle of the MACD signal line. By default, 9.

### `MACD-SHORT` 

Cycle of the MACD short line. By default, 12.

### `MACD-LONG` 

Cycle of the MACD long line. By default, 26.

### `RSI-CYCLE` 

Cycle of the RSI  formula. By default, 14.
