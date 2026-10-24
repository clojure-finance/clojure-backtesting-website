{:title "Automation"
 :date "2026-10-24"
 :layout :post
 :tags  []

:navbar? true
:page-index 9

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

An automation is a condition paired with an action. The backtester checks every automation's condition on each trading day, at the end of `next-date`, and runs the action when the condition is true. Automations make stop-loss and take-profit orders, or any other rule, run by themselves while your strategy steps through time.

The example on this page is the [`automation.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/automation.clj) worksheet, which runs on the bundled sample dataset.

## Set and Cancel Automations

#### set-automation

Register an automation.

| Argument          | Type     | Function                                       | Remarks |
| ----------------- | -------- | ---------------------------------------------- | ------- |
| `condition`       | Function | A function of no arguments, checked every trading day | Wrap it in `condition` so that `print-automation-list` can show its source. |
| `action`          | Function | A function of no arguments, run when the condition returns true | Wrap it in `action`, likewise. Orders placed by the action fill on the next trading day, as usual. |
| [`:max-dispatch`] | Integer  | How many times the action may run              | By default, unlimited. The automation is removed once used up. |
| [`:expiration`]   | Integer  | Number of trading days after today the automation stays active | By default, it never expires. |

**Return**

The automation's id, an integer.

**Example**

Buy 100 shares of AAA, then protect the position: a stop-sell if the price falls under $49, and a limit-sell to take profit above $52. Each fires at most once.

```clojure
(order "10001" 100 :print true)

(defn stop-sell
  "Sell qty shares once the price falls below prc."
  [permno prc qty]
  (set-automation
   (condition #(when-let [price (get-permno-price permno)] (< price prc)))
   (action #(order permno (- qty) :print true))
   :max-dispatch 1))

(defn limit-sell
  "Sell qty shares once the price rises above prc."
  [permno prc qty]
  (set-automation
   (condition #(when-let [price (get-permno-price permno)] (> price prc)))
   (action #(order permno (- qty) :print true))
   :max-dispatch 1))

(stop-sell "10001" 49 100)
(limit-sell "10001" 52 100)
;; => 2
```

<br>

#### cancel-automation

Remove an automation.

| Argument | Type    | Function                         | Remarks |
| -------- | ------- | -------------------------------- | ------- |
| `id`     | Integer | The id returned by `set-automation` |      |

**Return**

`true`

**Example**

```clojure
(cancel-automation 1)
;; => true
```

`init-portfolio` removes all automations.

<br>

## Inspect Automations

#### print-automation-list

Print the active automations in a table.

```clojure
(print-automation-list)
```

```text
| :id |                                                          :condition |                                     :action | :max-dispatch | :expire-date |
|-----+---------------------------------------------------------------------+---------------------------------------------+---------------+--------------|
|   1 | (fn* [] (when-let [price (get-permno-price permno)] (< price prc))) | (fn* [] (order permno (- qty) :print true)) |             1 |        Never |
|   2 | (fn* [] (when-let [price (get-permno-price permno)] (> price prc))) | (fn* [] (order permno (- qty) :print true)) |             1 |        Never |
```

<br>

#### print-automation-history

Print the days on which automations ran. Takes an optional number of rows, like `print-order-record`.

Running the example for 30 trading days:

```clojure
(dotimes [_ 30]
  (next-date))
```

```text
Order: 1990-01-03 | 10001 | 100.000000.
Automation 2 dispatched.
Order: 1990-01-19 | 10001 | -100.000000.
Automation 1 dispatched.
Order: 1990-01-30 | 10001 | -100.000000.
```

```clojure
(print-automation-history)
```

```text
|      :date | :automation |
|------------+-------------|
| 1990-01-18 |           2 |
| 1990-01-29 |           1 |
```

<br>

## Note: Automations Are Independent

The price first rose above $52, so the limit-sell (automation 2) sold the 100 shares. Later it fell below $49, and the stop-sell (automation 1) sold 100 shares again. Since the portfolio no longer held any, that second sale opened a short position:

```clojure
(print-order-record)
```

```text
|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-03 |   10001 |  50.81 | 50.81 |     100.0 |
| 1990-01-19 |   10001 |  51.85 | 51.85 |    -100.0 |
| 1990-01-30 |   10001 |  47.77 | 47.77 |    -100.0 |
```

```clojure
(print-portfolio)
```

```text
| :asset | :price |   :aprc | :quantity | :tot-val |
|--------+--------+---------+-----------+----------|
|   cash |    N/A |     N/A |       N/A | 14881.00 |
|  10001 |  48.01 | 48.0100 |    -100.0 | -4801.00 |
```

Automations do not know about each other. To make a pair where one cancels the other, cancel it in the action, or sell with `(order permno 0 :remaining true)`, which only closes whatever position is left:

```clojure
(action #(order "10001" 0 :remaining true :print true))
```
