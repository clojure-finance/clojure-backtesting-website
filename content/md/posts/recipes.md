{:title "Strategy Recipes"
 :date "2026-10-31"
 :layout :post
 :section "Examples"
 :lead "Short, reusable patterns for strategies that rank and rebalance: running a backtest as a function, momentum, combining factors, screening, logging, cash buffers and parameter sweeps."
 :toc true}

Each recipe is a small piece of Clojure you can copy into your own strategy. They build on the [Strategy Helpers](/posts/strategy/) and the [Fundamentals](/posts/fundamentals/) functions, and run in order in one `lein repl` session on the library's synthetic [Yahoo sample](/posts/yahoo-data/#try-it-offline):

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
(def stocks ["ALFA" "BETA" "GAMA" "DLTA"])   ; the universe; IDX is the benchmark
```

> **Watch out:** the sample has four stocks, three of them with fundamentals, and made-up prices. It shows how the code behaves, not which strategy is good. The numbers below are not evidence for any of the ideas.

## Run a backtest as a function

Once a backtest is a function, you can run it many times with different rules, which most of the recipes below do. `backtest` rebalances to the weights returned by `weights-fn` at the start of every period, and summarises the run against the IDX index with [`benchmark-stats`](/posts/evaluate/#benchmark-stats):

```clojure
(defn backtest
  "Rebalances to (weights-fn) at the start of every period from start to end,
   and summarises the run against the IDX index."
  [weights-fn & {:keys [period start end] :or {period :month start "2023-01-03" end "2024-12-31"}}]
  (init-portfolio start 100000)
  (while (neg? (compare (get-date) end))
    (when (new-period? period)
      (rebalance (weights-fn)))
    (next-date)
    (update-eval-report))
  (end-order)
  (let [{:keys [portfolio-return benchmark-return beta information-ratio]} (benchmark-stats "IDX")]
    {:final (format "$%,d" (long (portfolio-total)))
     :return (format "%.1f%%" (* 100 portfolio-return))
     :index (format "%.1f%%" (* 100 benchmark-return))
     :beta (format "%.2f" beta)
     :max-dd (format "%.1f%%" (* 100 (max-drawdown)))
     :ir (format "%.2f" information-ratio)}))
```

`weights-fn` is a function of no arguments, called on each rebalancing day, so it sees that day's data. The [Value Strategy](/posts/examples/example-value-strategy/), without its commission, becomes one line:

```clojure
(backtest #(equal-weights (top-n earnings-yield 2 :universe stocks)))
;; => {:final "$163,820", :return "63.8%", :index "19.4%", :beta "1.03", :max-dd "16.8%", :ir "0.99"}
```

`#(...)` makes the function; without it, the picks of the first day would be used for the whole run.

## Hold fixed weights

A fixed allocation, rebalanced every quarter: 60% in the index and 40% in GAMA. Rebalancing sells what has grown above its weight and buys what has fallen below it.

```clojure
(backtest (constantly {"IDX" 0.6 "GAMA" 0.4}) :period :quarter)
;; => {:final "$134,115", :return "34.1%", :index "19.4%", :beta "0.91", :max-dd "18.0%", :ir "0.85"}
```

The sample's index can be traded because it has prices; with real data, use an ETF such as `"SPY"`.

## Rank by momentum

*Momentum* buys what has risen most over the last months. Measure the rise with the **adjusted** price `:APRC`, which follows the total return through dividends and splits:

```clojure
(defn momentum
  "Total return over the last 126 trading days (about six months), from adjusted prices."
  [permno]
  (let [then (get-prev-n-date 126)
        a0 (when then (get-permno-by-key then permno :APRC))
        a1 (get-permno-by-key permno :APRC)]
    (when (and a0 a1)
      (- (/ a1 a0) 1.0))))
```

The same calculation on the raw price is a classic mistake:

```clojure
(defn raw-momentum [permno]   ; don't: a split looks like a crash
  (let [then (get-prev-n-date 126)
        p0 (when then (get-permno-price then permno))
        p1 (get-permno-price permno)]
    (when (and p0 p1)
      (- (/ p1 p0) 1.0))))
```

ALFA split 4-for-1 on 2024-08-01, so a month later its raw price is about a quarter of what it was six months before:

```clojure
(init-portfolio "2024-09-03" 100000)
(rank-by momentum :universe stocks)
;; => [["BETA" 0.39266599544274383] ["DLTA" 0.2922469019939975] ["ALFA" 0.21061914246218438] ["GAMA" 0.09593956671617465]]
(rank-by raw-momentum :universe stocks)
;; => [["BETA" 0.39266599544274294] ["DLTA" 0.29224690199399705] ["GAMA" 0.09593956671617287] ["ALFA" -0.697976981442608]]
```

On raw prices ALFA seems to have lost 70%; it actually gained 21%. Holding the top two each month:

```clojure
(backtest #(equal-weights (top-n momentum 2 :universe stocks)))
```

```text
Order request 2023-07-04 | GAMA | 22.193675538430284 fails due to initial margin requirement.
Order request 2024-03-04 | DLTA | 3.118915784529463 fails due to initial margin requirement.
{:final "$122,094", :return "22.1%", :index "19.4%", :beta "1.09", :max-dd "21.2%", :ir "0.06"}
```

Two purchases failed. The next recipe explains why.

## Keep a cash buffer

Weights that add up to 100% leave no room for the overnight move between sizing the orders (today's close) and filling them (the next close). When prices rise overnight, the last purchase of a rebalance costs more than the cash left, and if borrowing the difference would break the [initial margin](/posts/parameters/#initial-margin), it fails. Scaling the weights to 98% keeps a small buffer:

```clojure
(defn scaled [weights k] (update-vals weights #(* k %)))

(backtest #(scaled (equal-weights (top-n momentum 2 :universe stocks)) 0.98))
;; => {:final "$121,932", :return "21.9%", :index "19.4%", :beta "1.07", :max-dd "20.8%", :ir "0.06"}
```

No failures now. With three holdings, the raw-price mistake from the previous recipe also costs money, because ALFA drops out of the top three after its split:

```clojure
(backtest #(scaled (equal-weights (top-n momentum 3 :universe stocks)) 0.98))
;; => {:final "$150,437", :return "50.4%", :index "19.4%", :beta "1.05", :max-dd "18.4%", :ir "0.81"}
(backtest #(scaled (equal-weights (top-n raw-momentum 3 :universe stocks)) 0.98))
;; => {:final "$148,222", :return "48.2%", :index "19.4%", :beta "1.04", :max-dd "18.4%", :ir "0.75"}
```

## Combine factors by rank

Ratios come in different units, so adding them up is meaningless. Ranks can be added: rank the universe by each measure, add up each security's places, and sort by the total.

```clojure
(defn composite-rank
  "The securities of universe ranked by the sum of their ranks under each
   function of fs, best first. Securities missing from any ranking are left out."
  [fs universe]
  (let [ranks (for [f fs]
                (into {} (map-indexed (fn [i [p _]] [p i])) (rank-by f :universe universe)))
        ranked (filter (fn [p] (every? #(contains? % p) ranks)) universe)]
    (sort-by (fn [p] [(reduce + (map #(get % p) ranks)) p]) ranked)))

(init-portfolio "2024-11-15" 100000)
(composite-rank [earnings-yield book-to-market] stocks)
;; => ("ALFA" "BETA" "GAMA")
(composite-rank [earnings-yield book-to-market momentum] stocks)
;; => ("BETA" "ALFA" "GAMA")
```

ALFA has the highest earnings yield and BETA the highest book-to-market, so they tie on the first two measures; ties go to the permno that sorts first. Adding momentum puts BETA ahead. DLTA has no fundamentals, so it is left out. Held as a strategy:

```clojure
(backtest #(scaled (equal-weights (take 2 (composite-rank [earnings-yield book-to-market] stocks))) 0.98))
;; => {:final "$127,851", :return "27.9%", :index "19.4%", :beta "1.22", :max-dd "21.8%", :ir "0.17"}
```

To weight the measures differently, multiply each rank by a weight before adding.

## Screen, then weight

A screen is a predicate: keep the securities that pass, then weight them. Here, a return on equity above 10% and revenue that grew over the last fiscal year:

```clojure
(defn quality?
  "Return on equity above 10% and growing revenue."
  [permno]
  (let [roe (return-on-equity permno)
        g (growth permno :total-revenue)]
    (boolean (and roe g (> roe 0.10) (pos? g)))))

(init-portfolio "2024-11-15" 100000)
(filter quality? stocks)
;; => ("ALFA" "GAMA")
(backtest #(scaled (equal-weights (filter quality? stocks)) 0.98))
;; => {:final "$131,438", :return "31.4%", :index "19.4%", :beta "0.90", :max-dd "19.0%", :ir "0.37"}
```

Check for `nil` in screens: a company without the data should fail the test, not throw. Here `(and roe g ...)` does that. Screens and rankings combine: `(top-n earnings-yield 2 :universe (filter quality? stocks))`.

## Log what the strategy picked

A final value says little about *why*. Recording the picks on each rebalancing day, and keeping only the days they changed, shows what the strategy actually did:

```clojure
(defn picks-log
  "Runs backtest with picks-fn, equally weighted, and returns the days its picks changed."
  [picks-fn & opts]
  (let [log (atom [])]
    (apply backtest
           #(let [ps (sort (picks-fn))]
              (swap! log conj {:date (get-date) :picks ps})
              (scaled (equal-weights ps) 0.98))
           opts)
    (map first (partition-by :picks (deref log)))))

(clojure.pprint/print-table (picks-log #(top-n momentum 2 :universe stocks)))
```

```text
|      :date |          :picks |
|------------+-----------------|
| 2023-01-03 | ("BETA" "GAMA") |
| 2023-03-01 | ("DLTA" "GAMA") |
| 2023-04-03 | ("BETA" "GAMA") |
| 2023-06-01 | ("DLTA" "GAMA") |
| 2023-07-03 | ("ALFA" "GAMA") |
| 2023-08-01 | ("DLTA" "GAMA") |
| 2023-10-02 | ("BETA" "DLTA") |
| 2024-02-01 | ("DLTA" "GAMA") |
| 2024-03-01 | ("ALFA" "DLTA") |
| 2024-08-01 | ("BETA" "DLTA") |
| 2024-11-01 | ("BETA" "GAMA") |
| 2024-12-02 | ("BETA" "DLTA") |
```

Momentum changed its picks 11 times in 24 months, against 3 times for the value strategy. Each change sells one holding outright and buys another, and with commissions that adds up, as the next recipe shows. Sorting the picks keeps a reordering of the same two securities from counting as a change.

## Sweep a parameter

With the backtest as a function, trying several settings is a `for`. Holding one, two or three momentum stocks, rebalanced monthly or quarterly, with a 0.1% commission:

```clojure
(update-transaction-cost 0.001)
(def sweep
  (doall
   (for [period [:month :quarter]
         n [1 2 3]]
     (merge {:period period :n n}
            (select-keys (backtest #(scaled (equal-weights (top-n momentum n :universe stocks)) 0.98)
                                   :period period)
                         [:final :return :max-dd])))))
(update-transaction-cost 0.0)
(clojure.pprint/print-table sweep)
```

```text
|  :period | :n |   :final | :return | :max-dd |
|----------+----+----------+---------+---------|
|   :month |  1 | $171,617 |   71.6% |   22.4% |
|   :month |  2 | $120,352 |   20.4% |   21.0% |
|   :month |  3 | $149,237 |   49.2% |   18.5% |
| :quarter |  1 | $129,466 |   29.5% |   26.8% |
| :quarter |  2 | $106,822 |    6.8% |   26.8% |
| :quarter |  3 | $134,341 |   34.3% |   24.5% |
```

`doall` makes the `for` run before the commission is set back. The settings are applied before each `init-portfolio`, as [Parameters](/posts/parameters/) recommends.

> **Watch out:** six variants of one idea returned between 6.8% and 71.6%, with no pattern. Picking the best of many backtests and reporting only that one is called *overfitting* or *data snooping*: the winner is mostly luck, and will disappoint on new data. Decide on the rules before you look, test the winner on a period you did not use to choose it, and report how many variants you tried.

## More patterns

- **Stop-losses** on a rebalanced portfolio: register an [automation](/posts/automation/) per holding after each rebalance, and cancel them on the next one.
- **Event strategies** that trade on earnings announcements: see [Quality and Earnings](/posts/examples/example-quality-and-earnings/#buying-after-good-news).
- **Indicators as signals** in a ranking: any function of a permno works, for example `#(RSI %)` with `:ascending true` for the most oversold. Indicators use raw prices, so mind the splits.
