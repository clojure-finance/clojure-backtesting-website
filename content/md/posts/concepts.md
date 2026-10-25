{:title "Key Concepts"
 :date "2026-10-25"
 :layout :post
 :section "Learn"
 :lead "The ideas behind the library in plain words: the daily loop, when orders fill, which prices are used, how borrowing works and what the performance numbers mean."
 :toc true}

You do not need a finance background to use the backtester, but a few ideas come up on every page of this documentation. This page explains them once, with examples from the bundled sample dataset.

## What a backtest is

A **backtest** runs a trading strategy on historical data as if it were happening live. On each day the strategy may only use information that was available on that day. It places orders, the backtester fills them at realistic prices, and at the end you see how much the strategy would have made or lost, and how risky the ride was.

A backtest is a cheap way to find out that an idea does not work. It cannot prove that one will: markets change, and a strategy tuned to fit the past often fails on new data. Treat good results as a reason to test further, on other periods and other securities.

## The shape of every backtest

![The five steps of a backtest: load the data, start a portfolio, repeat the daily loop, close positions, review results.](/img/backtest-loop.svg)

1. **Load the data.** [`load-dataset`](/posts/api/#load-dataset) points the library at a directory of daily prices (the *main* dataset) and, optionally, company fundamentals (the *Compustat* dataset).
2. **Start a portfolio.** [`init-portfolio`](/posts/api/#init-portfolio) sets the start date and your starting cash. Calling it again starts over.
3. **Repeat for every trading day.** Your code looks at today's data, decides, places orders with [`order`](/posts/api/#order), and calls [`next-date`](/posts/api/#next-date) to move to the next trading day. The library has no built-in loop: you write it, usually with `while` or `dotimes`.
4. **Close positions.** [`end-order`](/posts/api/#end-order) sells everything at today's close and ends the run.
5. **Review results.** Print the [order record, portfolio and evaluation report](/posts/portfolio/), or [plot](/posts/evaluate/#plot) them.

Here is the smallest useful version:

```clojure
(load-dataset "resources/sample-data/main" "main" add-aprc)
(init-portfolio "1990-01-02" 10000)

(order "10001" 50)          ; buy 50 shares
(dotimes [_ 10]             ; let 10 trading days pass
  (next-date)
  (update-eval-report))
(end-order)                 ; sell everything

(print-portfolio-record -1)
```

## The clock: "today" and the date pointer

The backtester has a single global clock, the **date pointer**. [`get-date`](/posts/api/#get-date) tells you where it is. Every data function answers relative to it: [`get-info`](/posts/api/#get-info) returns today's rows, [`get-prev-n-days`](/posts/api/#get-prev-n-days) returns earlier ones, and indicators such as [`moving-avg`](/posts/indicators/#moving-avg) are computed from today backwards.

The clock only moves forward, one trading day per call to `next-date`, skipping weekends and holidays (any date not in the dataset). No function returns data from after "today", so your strategy cannot accidentally use tomorrow's prices. This mistake is called **look-ahead bias**, and it is the most common way backtests produce results that are too good to be true.

## When an order fills

An order does not fill when you place it. You place orders after seeing today's closing price, and you can no longer trade at a price that is already in the past. So the order becomes **pending**, and fills at the **close of the next trading day**:

![On 1990-01-02 the strategy orders 50 shares, which stays pending. next-date moves to 1990-01-03, where the order fills at the close of $50.81.](/img/order-timeline.svg)

This is why the order record of the [first backtest](/posts/get-started/#your-first-backtest) shows each order a day after it was placed. A few more rules:

- If the security has no price on the next day, the order waits, and expires after 3 trading days by default.
- An order is a number of shares. Positive buys, negative sells. With `:remaining true` the number is the holding you want to end up with, so `(order "10001" 0 :remaining true)` closes the position.
- On the same day, sales fill before purchases, so the cash from a sale can pay for a purchase.

The [Data and Trading Rules](/posts/user-guide/#trading-logic) page lists every rule.

## Securities and the sample dataset

Each security is identified by its **PERMNO**, a permanent number assigned by [CRSP](https://www.crsp.org/), the data provider the library is designed for. Permnos are strings in the API, such as `"10001"`. The documentation uses *permno* and *security* for the same thing; [`permno-tic` and `tic-permno`](/posts/api/#permno-tic-and-tic-permno) translate to and from ticker symbols.

The sample dataset in `resources/sample-data` has three made-up securities over January to March 1990, each with a quirk that exercises part of the library:

| Permno | Ticker | Price range | Quirk |
| ------ | ------ | ----------- | ----- |
| `10001` | AAA | about $44 to $52 | Pays a dividend on 1990-02-15 |
| `10002` | BBB | about $115 to $135, then $62 to $68 after the split | Splits 2-for-1 on 1990-03-01 |
| `10003` | CCC | about $18 to $21 | No prices from 1990-02-05 to 1990-02-09 |

## Prices: raw and adjusted

Every row has two prices that matter:

- **`:PRC`**, the raw closing price. Orders trade at this price, and the indicators are computed from it.
- **`:APRC`**, the *adjusted* price, added by `add-aprc`. It follows the security's total return, so dividends and splits do not make it jump.

The difference shows on BBB's split. After 1990-03-01 one old share became two new ones, so the raw price halved while the adjusted price carried on. Two fills from the [RSI example](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/relative_strength_index.clj), before and after the split:

```text
|      :date | :permno | :price |  :aprc | :quantity |
|------------+---------+--------+--------+-----------|
| 1990-02-22 |   10002 | 133.11 | 133.11 |      -5.0 |
| 1990-03-02 |   10002 |  66.92 | 133.84 |       5.0 |
```

The portfolio adjusts the share count on a split (5 shares become 10), and with reinvested dividends it also buys extra shares on dividend days, so neither event changes what your holding is worth. Indicators use the raw price, so a split looks like a crash to them. The [Golden Cross](/posts/examples/example-golden-cross/) example shows the effect.

## Long, short and borrowed money

A **long** position is shares you own: you profit if the price rises. A **short** position is shares you borrowed and sold: you profit if the price falls, because you can buy them back cheaper. In the portfolio a short position has a negative quantity.

You can also buy more than your cash pays for. The shortfall is a **loan**, shown as negative cash. Borrowed cash and borrowed shares both magnify gains and losses, so the backtester limits them:

| Term | Meaning |
| ---- | ------- |
| Equity, total value | Cash plus long positions minus short positions: what you would have after closing everything |
| Gross position value | Long positions plus short positions, both counted as positive |
| Leverage | (loan + short positions) / equity |
| Margin | equity / gross position value. 100% means no borrowing; lower means more borrowed |
| Initial margin | The margin a new borrowing order must keep, 50% by default. Orders that would go below it fail |
| Maintenance margin | The floor for the margin while you have borrowed, 25% by default. Below it, every position is closed (a *margin call*) and the run ends |

**Example.** With $300 of cash, buying 10 AAA shares at $50.81 costs $508.10. The portfolio borrows $208.10, so leverage is 208.10 / 300 = 0.69 and margin is 300 / 508.10 = 59%. That is above the initial margin of 50%, so the order goes through. The [Buying on Margin](/posts/examples/example-buying-on-margin/) example follows this portfolio further.

## Measuring performance

[`update-eval-report`](/posts/evaluate/#update-eval-report) adds today's metrics to the evaluation report. The metrics are:

| Metric | What it tells you |
| ------ | ----------------- |
| Daily return | How much the total value changed from yesterday, as a log return: ln(today / yesterday). Log returns add up over days |
| Total return | The sum of the daily log returns since the start |
| Volatility | How much the daily returns vary (their standard deviation). Higher means a bumpier ride |
| Sharpe ratio | Average daily return divided by volatility, scaled to a year (× √252). Return per unit of risk; above 1 is usually considered good |
| Maximum drawdown | The largest fall from a peak in total value, as a percentage of that peak. The worst loss you would have sat through |
| Profit per trade | (total value − starting cash) / number of filled orders, not counting the orders `end-order` places |

The rolling versions (`r-vol`, `r-sharpe`) use only the last 30 trading days, to show how risk changes over time. The [Evaluation](/posts/evaluate/) page has the exact definitions.

## Glossary

| Term | Meaning |
| ---- | ------- |
| Adjusted price (`:APRC`) | A price series that includes dividends and splits, so it moves with the total return |
| Automation | A condition and an action that the backtester checks every trading day; see [Automation](/posts/automation/) |
| Compustat | A database of company financial statements. The library joins it to the daily data by report date |
| CRSP | The Center for Research in Security Prices, the source of the daily data the library is designed for |
| Date pointer | The backtester's clock, the current "today" |
| Delisting | A security that stops trading. After 10 trading days without a price, a holding is turned into cash at its last value |
| Dividend | Cash a company pays to shareholders. Reinvested in more shares by default |
| Expiration | How many trading days a pending order waits for a price, 3 by default |
| Fill | The moment an order is executed, at the next trading day's close |
| Look-ahead bias | Using information in a backtest that was not available at the time; the date pointer prevents it |
| Pending order | An order placed but not yet filled |
| Permno | CRSP's permanent identifier for a security, such as `"10001"` |
| Split | A company dividing each share into several. The raw price falls, the adjusted price does not |
