{:title "Examples"
:date "2021-01-21"
:layout :post
:tags []

:navbar? true
:page-index 8

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

The [`examples`](https://github.com/clojure-finance/clojure-backtesting/tree/master/examples) folder of the library has these strategies as Gorilla REPL worksheets. They all run on the bundled sample dataset; open them with `make start` (see [Get Started](/posts/get-started#gorilla-repl)) or read them as plain Clojure files. Three of them are walked through step by step on this site.

| Example | Description |
| ------- | ----------- |
| [simple_trading_strategy.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/simple_trading_strategy.clj) | Basic buy/sell loop with order and portfolio output ([walkthrough](/posts/get-started#code-walkthrough)) |
| [golden_cross.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/golden_cross.clj) | 15/30-day moving average crossover ([walkthrough](/posts/examples/example-golden-cross)) |
| [bollinger_bands.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/bollinger_bands.clj) | Mean reversion using Bollinger Bands |
| [relative_strength_index.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/relative_strength_index.clj) | RSI-based overbought/oversold signals |
| [rate_of_change.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/rate_of_change.clj) | Momentum strategy using ROC |
| [buying_on_margin.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/buying_on_margin.clj) | Leveraged positions, short selling and margin requirements ([walkthrough](/posts/examples/example-buying-on-margin)) |
| [bad_strategy.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/bad_strategy.clj) | A losing short position through a dividend |
| [automation.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/automation.clj) | Stop and limit orders as automations ([walkthrough](/posts/automation)) |
| [Plotting_frame.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Plotting_frame.clj) | Line charts of security prices and returns |
| [Fundamental analysis.clj](https://github.com/clojure-finance/clojure-backtesting/blob/master/examples/Fundamental%20analysis.clj) | Filing visibility by report date and a return-on-equity screen ([walkthrough](/posts/examples/example-fundamental-analysis)) |
