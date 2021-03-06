{:title "Parameters"
 :layout :post
 :tags  []
 :toc true}


<style>
/* table styles */
table, th, td {
  border: 1px solid black;
  padding: 5px;
}
</style>

<br>

`ns: clojure-backtesting.parameters`

This namespace defines the parameters that are configurable in the backtester.

---

### Margin Requirements

<br>

`INITIAL-MARGIN`

This variable stores the minimum ratio of cash to total value of securities that must be paid when buying on margin. The default value is set as `0.5`.

**Example:**

```clojure
;; check the variable
(println INITIAL-MARGIN)

;; output:
0.5
```

<br>

`update-initial-margin`

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

`MAINTENANCE-MARGIN`

This variable stores the minimum ratio of cash to total value of securities that must be maintained in the margin account. If the portfolio margin goes below the maintenance margin, all positions will be automatically closed. The default value is set as `0.25`.

**Example:**

```clojure
;; check the variable
(println MAINTENANCE-MARGIN)

;; output:
0.25
```

<br>

`update-maintenance-margin`

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

<br>
