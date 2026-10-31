{:title "Example: Quality and Earnings"
 :date "2026-10-31"
 :layout :post
 :section "Examples"
 :lead "Two more uses of point-in-time fundamentals: Piotroski's F-score as a quality screen, and a strategy that buys after an earnings announcement beats the estimate."
 :toc true}

This walkthrough follows the worksheet [`quality_and_earnings.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/quality_and_earnings.clj). Like the [Value Strategy](/posts/examples/example-value-strategy/), it runs offline on the library's synthetic Yahoo snapshots.

**You will use:** [`piotroski-signals` and `piotroski-f-score`](/posts/fundamentals/#piotroski-f-score), [`fundamental-history`](/posts/fundamentals/#fundamental-history), [`growth`](/posts/fundamentals/#growth), [`latest-earnings`](/posts/fundamentals/#latest-earnings), [`order-target-percent`](/posts/strategy/#order-target-percent) and [`get-prev-n-date`](/posts/api/#get-prev-n-date).

## Setup

```clojure
(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
(init-portfolio "2024-06-03" 100000)
```

```text
Warning: ZZZZ has no prices: No data found, symbol may be delisted
Warning: BETA: 7 report dates are estimated from the reporting lag
"Date: 2024-06-03 Cash: $100000"
```

## A quality score: the F-score

Cheap stocks are often cheap for a reason. In 2000, the accounting professor Joseph Piotroski proposed a simple test of whether a company's finances are getting stronger or weaker: nine yes-or-no signals from its last two annual reports, covering profitability, cash flow, debt, liquidity, dilution, margins and efficiency. The **F-score** counts the yeses, from 0 (deteriorating on every front) to 9 (improving on every front). Among cheap stocks, he found that high scorers went on to do much better than low scorers.

`piotroski-signals` shows each signal; the [Fundamentals](/posts/fundamentals/#piotroski-f-score) page defines them.

```clojure
(piotroski-signals "GAMA")
```

```text
{:delta-liquidity false, :no-dilution true, :accrual true, :delta-margin false, :delta-leverage false, :cfo true, :delta-turnover false, :delta-roa false, :roa true}
```

GAMA is profitable (`:roa`), generates cash (`:cfo`), earns more cash than profit (`:accrual`) and did not issue shares (`:no-dilution`). Its liquidity, leverage, gross margin, return on assets and asset turnover did not improve. That makes four, which `piotroski-f-score` counts:

```clojure
(for [p ["ALFA" "BETA" "GAMA"]] {:security p :f-score (piotroski-f-score p)})
```

```text
({:security "ALFA", :f-score 4} {:security "BETA", :f-score 4} {:security "GAMA", :f-score 4})
```

> **Watch out:** the sample's companies grow at exactly the same rate every year, so their margins, turnover, returns on assets, leverage and liquidity are the same from one year to the next. Those signals neither rose nor fell, so they are false, and all three companies score 4. Real accounts change from year to year, and spread the scores out.

`piotroski-f-score` is `nil` unless all nine signals can be computed, which takes three fiscal years of total assets. In a strategy, use it as a screen: `(filter #(some-> (piotroski-f-score %) (>= 7)) universe)`.

## History and growth

`fundamental-history` lists what was public on the day, newest first. ALFA's revenue over its last four fiscal years:

```clojure
(map #(select-keys % [:period-end :value :public-date])
     (fundamental-history "ALFA" :total-revenue :annual 4))
```

```text
({:period-end "2023-12-31", :value 1.4641000000000005E9, :public-date "2024-02-12"} {:period-end "2022-12-31", :value 1.3310000000000005E9, :public-date "2023-02-10"} {:period-end "2021-12-31", :value 1.2100000000000002E9, :public-date "2022-02-10"} {:period-end "2020-12-31", :value 1.1E9, :public-date "2021-02-10"})
```

Revenue grew 10% a year, from $1.1 billion to $1.46 billion. `growth` computes the latest year-on-year change:

```clojure
{:revenue-growth (growth "ALFA" :total-revenue)
 :eps-growth (growth "ALFA" :diluted-eps)}
```

```text
{:revenue-growth 0.09999999999999987, :eps-growth 0.11111111111111116}
```

Earnings per share grew faster than revenue, by 11.1%, because net income grew 10% while the share count fell 1%.

## Buying after good news

When a company's results beat what analysts expected, its share price tends to keep drifting up for weeks afterwards, a long-studied effect called *post-earnings-announcement drift*. This strategy tries to catch it:

- On the day an announcement becomes public with a positive surprise, put 30% of the portfolio into the company.
- Sell 20 trading days later.

`latest-earnings` returns the latest announcement public on the backtest date, with `:surprise-pct`, how far the actual EPS beat (or missed) the estimate in percent. Comparing its `:public-date` with today finds the announcement days:

```clojure
(init-portfolio "2023-01-03" 100000)

(def bought (atom {})) ; permno -> date bought

(while (neg? (compare (get-date) "2024-12-31"))
  (doseq [p ["ALFA" "BETA" "GAMA"]]
    (let [e (latest-earnings p)]
      (when (and e (= (:public-date e) (get-date)) (pos? (or (:surprise-pct e) 0.0))
                 (not (contains? (deref bought) p)))
        (order-target-percent p 0.3)
        (swap! bought assoc p (get-date)))))
  (doseq [[p d] (deref bought)]
    (when (= d (get-prev-n-date 20))
      (order p 0 :remaining true)
      (swap! bought dissoc p)))
  (next-date)
  (update-eval-report))
(end-order)
(print-order-record 10)
```

```text
|      :date | :permno |   :price |  :aprc |  :quantity |
|------------+---------+----------+--------+------------|
| 2023-02-13 |    ALFA |  367.336 | 368.72 |    81.2456 |
| 2023-02-15 |    GAMA |  55.6942 |  55.69 |   547.6275 |
| 2023-02-21 |    BETA |   8.0031 |   8.00 |  3729.5818 |
| 2023-03-13 |    ALFA |  334.448 | 336.09 |   -81.3361 |
| 2023-03-15 |    GAMA |  55.0107 |  55.01 |  -547.6275 |
| 2023-03-21 |    BETA |   8.0405 |   8.04 | -3729.5818 |
| 2023-05-01 |    GAMA |  59.1238 |  59.12 |   491.1352 |
| 2023-05-02 |    ALFA | 362.5512 | 364.33 |    81.5209 |
| 2023-05-09 |    BETA |    9.184 |   9.18 |  3157.1325 |
| 2023-05-29 |    GAMA |  60.8056 |  60.81 |  -491.1352 |
```

ALFA's fourth-quarter results became public on 2023-02-10, so the strategy ordered that day and the order filled at the next close, on 2023-02-13. The sale 20 trading days later is a little larger than the purchase (81.3361 against 81.2456 shares) because ALFA's dividend on 2023-02-14 was reinvested.

```clojure
(last (deref eval-report-data))
```

```text
{:date "2024-12-31", :tot-value "$92246", :vol "0.4041%", :r-vol "0.1472%", :sharpe "-0.6085", :r-sharpe "-5.6701", :pnl-pt "$-215", :max-drawdown "8.1014%"}
```

The strategy lost 7.8% over two years. That is the expected answer here: the sample's prices are random walks that do not react to the reports, and every announcement in it beats the estimate by the same 3.09%, so the strategy simply buys after every report. On real data, the same code tests whether the drift exists for your universe and period.

## Takeaways

- `latest-earnings` and `days-since-earnings` make event strategies a few lines long. Use `:surprise-pct` rather than the EPS figures, which are Yahoo's adjusted numbers in an inconsistent share basis.
- An announcement after the close becomes public the next trading day, and the order fills the day after that. The backtest cannot buy at the price before the news.
- **Try it:** buy only surprises above 5%, hold for 10 or 40 days, or combine both parts: only buy after good news when the F-score is 6 or more.
