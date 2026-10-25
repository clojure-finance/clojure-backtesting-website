{:title "Example: Bollinger Bands"
 :date "2026-10-25"
 :layout :post
 :section "Examples"
 :lead "A mean-reversion strategy: buy when the price falls unusually far below its recent average, sell when it rises unusually far above."
 :toc true}

This walkthrough follows the worksheet [`bollinger_bands.clj`](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/bollinger_bands.clj). It runs on the bundled sample dataset; open it in Gorilla REPL with `make start` (see [Get Started](/posts/get-started/#option-2-gorilla-repl)), or paste the code into `lein repl`.

**You will use:** [`moving-avg`](/posts/indicators/#moving-avg), [`moving-sd`](/posts/indicators/#moving-sd), [`order`](/posts/api/#order) with `:remaining`, and the [evaluation report](/posts/evaluate/).

## The idea

Bollinger Bands draw an envelope around the price:

- **Middle band**: the 20-day simple moving average (SMA) of the closing price.
- **Upper band**: the SMA plus 2 standard deviations of the price over the same 20 days.
- **Lower band**: the SMA minus 2 standard deviations.

The bands widen when the price is volatile and narrow when it is calm. A *mean-reversion* trader reads a close below the lower band as "oversold", likely to bounce back, and a close above the upper band as "overbought". The strategy here:

- **Buy** 10 shares whenever the price closes below the lower band.
- **Sell** the whole position whenever the price closes above the upper band.

It trades AAA (`10001`) and CCC (`10003`). The bands need 20 closes, so nothing happens before 1990-01-29, the 20th trading day. CCC's five days without prices in February mean its bands are missing for a while after them, too.

## Setup

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)
(init-portfolio "1990-01-02" 10000)
```

```text
The dataset is already furnished by add-aprc. No more modification is needed.
"Date range: 1990-01-02 ~ 1990-03-30"
"Date: 1990-01-02 Cash: $10000"
```

## The strategy

`bollinger-bands` handles one security on the current day. [`moving-avg`](/posts/indicators/#moving-avg) and [`moving-sd`](/posts/indicators/#moving-sd) return `nil` until 20 prices are available, and `when` with `and` skips those days.

```clojure
(defn bollinger-bands [permno]
  (let [price (get-permno-price permno)
        sma   (moving-avg permno 20)
        sd20  (moving-sd permno 20)]
    (when (and price sma sd20)
      (let [upper (+ sma (* 2 sd20))
            lower (- sma (* 2 sd20))]
        (when (< price lower)
          (order permno 10))                   ; buy 10 more shares
        (when (> price upper)
          (order permno 0 :remaining true))))))  ; sell everything
```

The main loop runs it for both securities each day, records the metrics and moves the clock. On 1990-03-29, the last full day of the sample, `end-order` sells what is left.

```clojure
(while (< (compare (get-date) "1990-03-29") 0)
  (bollinger-bands "10001")
  (bollinger-bands "10003")
  (update-eval-report)
  (next-date))
(end-order)
```

## What happened

Six closes crossed a band. These are the values on those days:

| Day | Security | Close | Lower band | Upper band | Signal | What the order did |
| --- | -------- | ----- | ---------- | ---------- | ------ | ------------------ |
| 1990-01-30 | AAA | 47.77 | 48.06 | 52.29 | Buy | Bought 10 on 1990-01-31 at 48.05 |
| 1990-03-19 | AAA | 45.67 | 46.15 | 49.53 | Buy | Bought 10 on 1990-03-20 at 44.69 |
| 1990-03-20 | AAA | 44.69 | 45.51 | 49.90 | Buy | Bought 10 on 1990-03-21 at 44.85 |
| 1990-03-20 | CCC | 19.24 | 18.04 | 19.23 | Sell | Nothing: no CCC held |
| 1990-03-21 | AAA | 44.85 | 45.03 | 49.98 | Buy | Bought 10 on 1990-03-22 at 44.91 |
| 1990-03-21 | CCC | 19.82 | 17.90 | 19.45 | Sell | Nothing: no CCC held |

Each order fills at the close of the trading day after its signal. CCC's two sell signals do nothing, because "sell down to 0 shares" (`:remaining true`) on a position of 0 shares has nothing to trade. AAA never closed above its upper band, so its position was only sold by `end-order`:

```clojure
(print-order-record)
```

```text
|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-31 |   10001 |  48.05 | 48.05 |      10.0 |
| 1990-03-20 |   10001 |  44.69 | 45.15 |      10.0 |
| 1990-03-21 |   10001 |  44.85 | 45.31 |      10.0 |
| 1990-03-22 |   10001 |  44.91 | 45.37 |      10.0 |
| 1990-03-29 |   10001 |  43.87 | 44.32 |  -40.1025 |
```

The last sale is 40.1025 shares, not 40. AAA paid a dividend on 1990-02-15 and dividends are reinvested by default, so the first 10 shares grew to 10.1025. The adjusted price (`:aprc`) is above the raw price after that date for the same reason (see [Prices: raw and adjusted](/posts/concepts/#prices-raw-and-adjusted)).

```clojure
(print-portfolio)
```

```text
| :asset | :price | :aprc | :quantity | :tot-val |
|--------+--------+-------+-----------+----------|
|   cash |    N/A |   N/A |       N/A |  9934.30 |
```

## Evaluation report

The report starts on 1990-01-31, the day of the first fill. Its first ten rows:

```clojure
(print-eval-report 10)
```

```text
|      :date | :tot-value |    :vol |  :r-vol | :sharpe | :r-sharpe | :pnl-pt | :max-drawdown |
|------------+------------+---------+---------+---------+-----------+---------+---------------|
| 1990-01-31 |     $10000 | 0.0000% | 0.0000% |  0.0000 |    0.0000 |      $0 |       0.0000% |
| 1990-02-01 |      $9996 | 0.0083% | 0.0083% | -3.3101 |   -3.3101 |     $-4 |       0.0400% |
| 1990-02-02 |      $9995 | 0.0082% | 0.0082% | -3.3240 |   -3.3240 |     $-4 |       0.0410% |
| 1990-02-05 |     $10000 | 0.0128% | 0.0128% |  0.3485 |    0.3485 |      $0 |       0.0410% |
| 1990-02-06 |     $10008 | 0.0199% | 0.0199% |  2.6432 |    2.6432 |      $8 |       0.0410% |
| 1990-02-07 |     $10000 | 0.0258% | 0.0258% |  0.0228 |    0.0228 |      $0 |       0.0849% |
| 1990-02-08 |      $9994 | 0.0275% | 0.0275% | -1.1350 |   -1.1350 |     $-5 |       0.1409% |
| 1990-02-09 |     $10002 | 0.0309% | 0.0309% |  0.4252 |    0.4252 |      $2 |       0.1409% |
| 1990-02-12 |      $9998 | 0.0313% | 0.0313% | -0.2709 |   -0.2709 |     $-1 |       0.1409% |
| 1990-02-13 |      $9999 | 0.0308% | 0.0313% | -0.0665 |   -0.0675 |      $0 |       0.1409% |
```

With only $480 invested in February, the daily changes are tiny compared with the $10,000 portfolio, so the volatility is a few hundredths of a percent.

## Takeaways

- The strategy ended with $9,934.30, a loss of $65.70. AAA kept falling through March, and buying each dip below the lower band added to a losing position. Mean reversion assumes the price comes back; in a steady downtrend it does not.
- Signals can repeat on consecutive days. Here, three days in a row below the band bought 30 shares. To buy once per dip, check whether you already hold the security, for example with `(get-in @portfolio ["10001" :quantity])`.
- **Try it:** change the 2 standard deviations to 1.5, or trade BBB (`10002`) instead, and compare the evaluation reports. Remember that BBB's split on 1990-03-01 halves its raw price, which the bands will see as a crash.
