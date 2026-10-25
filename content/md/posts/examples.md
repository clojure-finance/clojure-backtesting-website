{:title "Examples"
 :date "2021-01-21"
 :layout :post
 :section "Examples"
 :lead "Complete strategies that run on the bundled sample dataset, from a simple buy-and-sell loop to margin trading and fundamentals."
 :toc true}

Every example is a Gorilla REPL worksheet in the [`examples`](https://github.com/clojure-finance/clojure-backtesting/tree/master/examples) folder of the library. Open them with `make start` (see [Get Started](/posts/get-started/#option-2-gorilla-repl)), or read them as plain Clojure files and paste the code into `lein repl`. The outputs shown on this site come from running them.

## Step-by-step walkthroughs

These pages explain the code and the results line by line. If you are new, read them roughly in this order.

<div class="card-grid">
<a class="card" href="/posts/get-started/#your-first-backtest"><span class="step">1</span><span class="card-title">A first strategy</span><span class="card-text">Buy once, sell a little every other day, and read the order record, portfolio and evaluation report.</span><span class="more">Basics →</span></a>
<a class="card" href="/posts/examples/example-golden-cross/"><span class="step">2</span><span class="card-title">Golden Cross</span><span class="card-text">Trend following with two moving averages, on two securities at once.</span><span class="more">Moving averages →</span></a>
<a class="card" href="/posts/examples/example-bollinger-bands/"><span class="step">3</span><span class="card-title">Bollinger Bands</span><span class="card-text">Mean reversion: buy far below the average, sell far above it. Shows why buying every dip can go wrong.</span><span class="more">Volatility bands →</span></a>
<a class="card" href="/posts/automation/"><span class="step">4</span><span class="card-title">Stop and limit orders</span><span class="card-text">Automations that sell by themselves when the price crosses a level.</span><span class="more">Automation →</span></a>
<a class="card" href="/posts/examples/example-buying-on-margin/"><span class="step">5</span><span class="card-title">Buying on Margin</span><span class="card-text">Borrowed cash, short selling, and what the initial and maintenance margins do.</span><span class="more">Leverage →</span></a>
<a class="card" href="/posts/examples/example-fundamental-analysis/"><span class="step">6</span><span class="card-title">Fundamental Analysis</span><span class="card-text">Rank securities by return on equity, using only the filings that were public at the time.</span><span class="more">Fundamentals →</span></a>
</div>

## All worksheets

| Worksheet | What it shows | Walkthrough |
| --------- | ------------- | ----------- |
| [simple_trading_strategy.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/simple_trading_strategy.clj) | A basic buy and sell loop with order and portfolio output | [Get Started](/posts/get-started/#your-first-backtest) |
| [golden_cross.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/golden_cross.clj) | 15-day and 30-day moving average crossover | [Golden Cross](/posts/examples/example-golden-cross/) |
| [bollinger_bands.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/bollinger_bands.clj) | Mean reversion with Bollinger Bands | [Bollinger Bands](/posts/examples/example-bollinger-bands/) |
| [relative_strength_index.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/relative_strength_index.clj) | Overbought and oversold signals from the RSI | |
| [rate_of_change.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/rate_of_change.clj) | A momentum strategy using the rate of change | |
| [automation.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/automation.clj) | Stop and limit orders as automations | [Automation](/posts/automation/) |
| [buying_on_margin.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/buying_on_margin.clj) | Leveraged positions, short selling and margin requirements | [Buying on Margin](/posts/examples/example-buying-on-margin/) |
| [bad_strategy.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/bad_strategy.clj) | A losing short position through a dividend | |
| [Plotting_frame.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Plotting_frame.clj) | Line charts of security prices and returns | |
| [Fundamental analysis.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Fundamental%20analysis.clj) | Filing visibility by report date and a return-on-equity screen | [Fundamental Analysis](/posts/examples/example-fundamental-analysis/) |

## Ideas to try

Once an example runs, change one thing at a time and compare the evaluation reports:

- Change the windows: 10 and 20 days instead of 15 and 30 in the Golden Cross, or 1.5 standard deviations instead of 2 in the Bollinger Bands.
- Trade a different security, or all of them with `(available-permnos)`.
- Add costs with `(update-transaction-cost 0.001)` before `init-portfolio`, and see how much of the profit survives.
- Protect each position with a stop-loss [automation](/posts/automation/).
