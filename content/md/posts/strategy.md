{:title "Strategy Helpers"
 :date "2026-10-31"
 :layout :post
 :section "API Reference"
 :lead "Calendar triggers, ranking and screening, and orders that target a fraction of the portfolio: the building blocks of strategies that pick from many securities."
 :toc true}

The strategies on the other pages mostly watch one security and trade a number of shares when a signal fires. Many real strategies are *cross-sectional* instead: every month or quarter they rank a universe of securities, hold the best ones, and give each a target share of the portfolio. `clojure-backtesting.strategy` turns that into a few lines:

```clojure
(while (neg? (compare (get-date) "2024-12-31"))
  (when (new-period? :month)                                   ; on the first trading day of each month
    (rebalance (equal-weights (top-n earnings-yield 2))))       ; hold the two cheapest, half each
  (next-date)
  (update-eval-report))
(end-order)
```

The examples run on the library's synthetic [Yahoo sample](/posts/yahoo-data/#try-it-offline), from 2024-11-15:

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
(init-portfolio "2024-11-15" 100000)
```

The helpers do not need Yahoo data: ranking works with any function of a security, such as an [indicator](/posts/indicators/), and the order functions work on any dataset.

## When to trade

### `new-period?`

`(new-period? unit)`

True on the first trading day of a new `:week`, `:month`, `:quarter` or `:year` in the dataset, and on the first day of the run, so that a strategy invests at once instead of waiting for the next period. "First trading day" means the first date in the dataset, so holidays are handled.

```clojure
(map new-period? [:week :month :quarter :year])
;; => (true true true true)      2024-11-15 is the first day of the run
(next-date)
;; => "2024-11-18"
(map new-period? [:week :month :quarter :year])
;; => (true false false false)   a Monday
```

Any other unit throws: `"Unknown period :day, expected :week, :month, :quarter or :year."` To trade every day, just leave out the check.

## Ranking and screening

### `rank-by`

`(rank-by f & options)`

Ranks securities by `(f permno)`, highest first, as `[[permno value] ...]`. Securities for which `f` returns `nil`, or a value that is not a finite number, are left out, so `f` can simply return `nil` when it has no opinion.

| Option | Default | Meaning |
| ------ | ------- | ------- |
| `:universe` | `(available-permnos)` | The securities to rank |
| `:ascending` | `false` | `true` ranks the lowest value first |

```clojure
(rank-by earnings-yield)
;; => [["ALFA" 0.05366868843586461] ["BETA" 0.03332584981650266] ["GAMA" 0.027329231371534268]]
(rank-by pe-ratio :ascending true)
;; => [["ALFA" 18.632838423003843] ["BETA" 30.006736677568803] ["GAMA" 36.59085710846539]]
(rank-by book-to-market :universe ["ALFA" "BETA" "DLTA"])
;; => [["BETA" 0.39307591783985635] ["ALFA" 0.33069281706309384]]
```

DLTA and IDX have no statements, so the ratios are `nil` for them and they drop out. Ties are broken by permno, so results are reproducible.

### `top-n`

`(top-n f n & options)`

The permnos of the `n` securities with the highest `(f permno)`, or the lowest with `:ascending true`. Takes the options of `rank-by`.

```clojure
(top-n earnings-yield 2)                 ;; => ["ALFA" "BETA"]
(top-n pe-ratio 1 :ascending true)       ;; => ["ALFA"]
```

To screen before ranking, filter the universe first. For example, the cheapest companies among those with a return on equity above 10%:

```clojure
(top-n earnings-yield 2
       :universe (filter #(some-> (return-on-equity %) (> 0.10)) (available-permnos)))
;; => ["ALFA" "GAMA"]
```

Any function of a permno works, including your own. The [Strategy Recipes](/posts/recipes/) page ranks by momentum computed from adjusted prices, and combines several ratios into one score.

## Weights

A target portfolio is a map from permno to its fraction of equity, such as `{"ALFA" 0.5 "BETA" 0.5}`. What the weights leave over stays in cash; weights above 1 in total borrow cash, and a negative weight is a short position.

### `equal-weights`

`(equal-weights permnos)`

`{permno 1/n}` for the given securities; duplicates count once, and no securities give `{}`.

```clojure
(equal-weights ["ALFA" "GAMA"])   ;; => {"ALFA" 0.5, "GAMA" 0.5}
(equal-weights [])                ;; => {}
```

Other weightings are ordinary Clojure. Market-cap weights, for example:

```clojure
(defn cap-weights [permnos]
  (let [caps (into {} (keep (fn [p] (when-let [c (market-cap p)] [p c]))) permnos)
        total (reduce + (vals caps))]
    (update-vals caps #(/ % total))))

(cap-weights (available-permnos))
;; => {"ALFA" 0.23022370075470155, "BETA" 0.11010479921892811, "GAMA" 0.6596715000263703}
```

## Target orders

### `order-target-percent`

`(order-target-percent permno weight & options)`

Orders `permno` so that it becomes `weight` of today's equity, at today's price. Like every order, it fills at the next close, and the trade is the difference to the holding at that time, so calling it for a security you already hold only trades the difference. Returns the target quantity, or `nil` (and orders nothing) when the security has no price today.

| Option | Default | Meaning |
| ------ | ------- | ------- |
| `:whole-shares?` | `false` | Round the target toward zero to whole shares |
| `:expiration`, `:leverage`, `:print`, `:direct` | as for [`order`](/posts/api/#order) | Passed on to the order |

On 2024-11-15, with $100,000 of equity and DLTA at $44.5727:

```clojure
(order-target-percent "DLTA" 0.1 :whole-shares? true)   ;; => 224.0
(order-target-percent "NOPE" 0.1)                       ;; => nil, no price today
```

### `rebalance`

`(rebalance weights & options)`

Targets the whole portfolio: every security in `weights` gets an `order-target-percent`, and every holding **not** in `weights` is closed. Takes the options of `order-target-percent`, and returns `{permno target-quantity}` for the securities in `weights` that have a price today; the orders that close other holdings are not in it.

```clojure
(init-portfolio "2024-11-15" 100000)
(rebalance (equal-weights (top-n earnings-yield 2)))
;; => {"ALFA" 433.93511455019154, "BETA" 3318.180309918041}
(next-date)
(print-portfolio)
```

```text
| :asset |   :price |    :aprc | :quantity | :tot-val |
|--------+----------+----------+-----------+----------|
|   cash |      N/A |      N/A |       N/A |  2574.15 |
|   ALFA | 112.0239 | 453.5382 |  433.9351 | 48611.10 |
|   BETA |  14.7113 |  14.7113 | 3318.1803 | 48814.75 |
```

The next day, a new target without BETA sells all of BETA, trims ALFA and buys GAMA. Sales fill before purchases, so their proceeds pay for GAMA:

```clojure
(rebalance {"ALFA" 0.3 "GAMA" 0.3})
;; => {"ALFA" 267.79999625080006, "GAMA" 371.1507032687242}
(next-date)
(print-order-record)
```

```text
|      :date | :permno |   :price |  :aprc |  :quantity |
|------------+---------+----------+--------+------------|
| 2024-11-18 |    ALFA | 112.0239 | 453.54 |   433.9351 |
| 2024-11-18 |    BETA |  14.7113 |  14.71 |  3318.1803 |
| 2024-11-19 |    ALFA | 111.8384 | 452.79 |  -166.1351 |
| 2024-11-19 |    BETA |  14.9191 |  14.92 | -3318.1803 |
| 2024-11-19 |    GAMA |  80.0449 |  80.04 |   371.1507 |
```

A negative weight sells short, within the [margin rules](/posts/concepts/#long-short-and-borrowed-money). Half long ALFA, half short GAMA:

```clojure
(init-portfolio "2024-11-15" 100000)
(rebalance {"ALFA" 0.5 "GAMA" -0.5})
;; => {"ALFA" 433.93511455019154, "GAMA" -597.1760737822982}
(next-date)
(print-portfolio)
```

```text
| :asset |   :price |    :aprc | :quantity |  :tot-val |
|--------+----------+----------+-----------+-----------|
|   cash |      N/A |      N/A |       N/A |  99658.46 |
|   GAMA |  80.8297 |  80.8297 | -597.1761 | -48269.56 |
|   ALFA | 112.0239 | 453.5382 |  433.9351 |  48611.10 |
```

### `portfolio-equity` and `target-quantity`

`(portfolio-equity)` is the portfolio's equity as last valued: today's close once `next-date` has run, or the starting capital on the first day. `(target-quantity permno weight whole-shares?)` is the number of shares `order-target-percent` would aim for, without ordering.

On 2024-11-18, the day after a fresh start with nothing bought, and ALFA at $112.0239:

```clojure
(init-portfolio "2024-11-15" 100000)
(next-date)                             ;; => "2024-11-18"
(portfolio-equity)                      ;; => 100000.0
(target-quantity "ALFA" 0.25 false)     ;; => 223.1666635423334
(target-quantity "ALFA" 0.25 true)      ;; => 223.0
```

## Things to know

**Weights drift between sizing and filling.** Targets are sized at today's close, but the orders fill at the next close. In the first `rebalance` above, both prices fell overnight, so $2,574 of the $100,000 stayed in cash. When prices rise instead, the purchases cost a little more than the cash available, and the portfolio borrows the difference.

**A fully invested rebalance can fail a purchase.** Borrowing to buy must meet the [initial margin](/posts/parameters/#initial-margin): with the default of 50%, your own cash has to pay for at least half of each purchase. When a rebalance has spent nearly all the cash, a last small top-up can fail. The [momentum recipe](/posts/recipes/#keep-a-cash-buffer), fully invested, prints:

```text
Order request 2023-07-04 | GAMA | 22.193675538430284 fails due to initial margin requirement.
```

That purchase is skipped, so the portfolio holds slightly less than its target until the next rebalance. To avoid it, leave a small cash buffer, for example by scaling the weights to 98%:

```clojure
(rebalance (update-vals (equal-weights (top-n earnings-yield 2)) #(* 0.98 %)))
```

**Shares are fractional by default.** Pass `:whole-shares? true` for whole shares.

**Every trade pays the commission.** With [`update-transaction-cost`](/posts/parameters/#update-transaction-cost) set, a monthly rebalance trades every holding a little every month. The [parameter sweep](/posts/recipes/#sweep-a-parameter) recipe compares rebalancing monthly and quarterly.

**Compare with a benchmark.** [`benchmark-stats`](/posts/evaluate/#benchmark-stats) compares the run with an index or ETF in the dataset: returns, beta, alpha, tracking error and information ratio.
