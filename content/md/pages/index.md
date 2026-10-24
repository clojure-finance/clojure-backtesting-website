{:title "Welcome to the Clojure Backtesting Library"
 :layout :page
 :page-index 0
 :navbar? true
 :home? true}

Welcome to the Clojure Backtesting Library! This is an open-source library for quantitative finance and trading developed at the HKU Business School. 

## Features

- **Day-by-day simulation**  
  Step through trading days, place orders, and track the portfolio. Orders fill at the next close, like in the real market.

- **Support for large data files**  
  Data is read from disk day by day with a cache, so datasets larger than memory work. The library has been tested with data up to 30 GB.

- **Built-in indicators**  
  EMA, MACD, RSI, Rate of Change, parabolic SAR, ATR and Keltner Channels, plus moving averages and standard deviations for crossovers or Bollinger Bands.

- **Margin and short selling**  
  Borrow to buy or sell short, with configurable initial and maintenance margins, transaction costs and loan interest.

- **Dividends, splits and fundamentals**  
  Dividends are reinvested or paid to cash, and Compustat-style fundamentals are joined by report date, so a strategy only sees what had been published.

- **Evaluation, automation and plotting**  
  Sharpe ratio, volatility and maximum drawdown; condition/action rules such as stop-loss orders; line charts in the browser.

- **Sample data included**  
  A small bundled dataset lets you try every function right after cloning.

## Video

Here is an introduction video to the backtester, which includes installation guide, example walkthrough and learning tips.

<iframe width="672" height="378" src="https://www.youtube.com/embed/7F4gUWOhkB8?si=n3E9T370qlv3s0TU"> </iframe>

## Table of Contents

If you are new to language Clojure or backtesters in general, you are recommended to go through the below documents one by one in order. This is to ensure that you have a flat learning curve.

### Part I: Installation & Pre-requisites

- [Get Started](/posts/get-started)
- [User Guide](/posts/user-guide)

### Part II: API documentation

- [Basic APIs](/posts/api)
- [Portfolio](/posts/portfolio)
- [Evaluate](/posts/evaluate)
- [Indicators](/posts/indicators)
- [Parameters](/posts/parameters)
- [Automation](/posts/automation)

### Part III: Examples

- [Examples Archive](/posts/examples)
