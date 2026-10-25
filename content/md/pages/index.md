{:title "Overview"
 :layout :home
 :page-index 0
 :home? true}

## What you can do

A backtest replays history: your strategy sees the market one day at a time, places orders, and the library keeps the books. These are the parts it handles for you.

<div class="card-grid">
<div class="card"><span class="icon"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg></span><h3>Day-by-day simulation</h3><p>A global clock moves through trading days. Your code only sees data up to "today", so it cannot peek at the future.</p></div>
<div class="card"><span class="icon green"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><path d="M22 4 12 14.01l-3-3"/></svg></span><h3>Orders fill like a broker's</h3><p>An order fills at the next trading day's close. Orders wait while a security has no price, and expire after a few days.</p></div>
<div class="card"><span class="icon amber"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M23 18 13.5 8.5l-5 5L1 6"/><path d="M17 18h6v-6"/></svg></span><h3>Margin and short selling</h3><p>Borrow cash or shares under initial and maintenance margins, with optional commissions and loan interest.</p></div>
<div class="card"><span class="icon"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span><h3>Dividends, splits, fundamentals</h3><p>Dividends are reinvested or paid to cash. Company filings are joined by report date, so a strategy only sees published numbers.</p></div>
<div class="card"><span class="icon green"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span><h3>Technical indicators</h3><p>Moving averages, EMA, MACD, RSI, rate of change, parabolic SAR, ATR and Keltner Channels.</p></div>
<div class="card"><span class="icon amber"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 20V10M12 20V4M6 20v-6"/></svg></span><h3>Performance metrics</h3><p>Daily returns, volatility, Sharpe ratio, maximum drawdown and profit per trade, plus line charts in the browser.</p></div>
<div class="card"><span class="icon"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M13 2 3 14h9l-1 8 10-12h-9l1-8z"/></svg></span><h3>Automated rules</h3><p>Pair a condition with an action, such as a stop-loss, and the backtester checks it every trading day.</p></div>
<div class="card"><span class="icon green"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg></span><h3>Data larger than memory</h3><p>Data is read from disk one day at a time, with a cache. It has been tested with datasets of up to 30 GB.</p></div>
</div>

## How a backtest works

Every backtest follows the same four steps. The [Key Concepts](/posts/concepts/) page explains each step in more detail.

<div class="card-grid">
<div class="card"><span class="step">1</span><h3>Load the data</h3><p><code>load-dataset</code> points the library at daily prices, and optionally at company fundamentals.</p></div>
<div class="card"><span class="step">2</span><h3>Start a portfolio</h3><p><code>init-portfolio</code> sets the start date and the cash you begin with.</p></div>
<div class="card"><span class="step">3</span><h3>Step through the days</h3><p>Each day, read prices or indicators, call <code>order</code>, then <code>next-date</code> to move on.</p></div>
<div class="card"><span class="step">4</span><h3>Review the results</h3><p><code>end-order</code> closes the positions. The records and the evaluation report show what happened.</p></div>
</div>

## What the example above produced

The strategy at the top of this page runs on the sample dataset that ships with the library. It holds 50 shares of the security AAA (`10001`) whenever the price closes above its 10-day average, and none otherwise.

<div class="stats">
<div class="stat"><span class="label">Starting cash</span><span class="value">$10,000</span></div>
<div class="stat"><span class="label">Final value</span><span class="value down">$9,652</span></div>
<div class="stat"><span class="label">Orders filled</span><span class="value">14</span></div>
<div class="stat"><span class="label">Sharpe ratio</span><span class="value down">−4.72</span></div>
<div class="stat"><span class="label">Max drawdown</span><span class="value">4.09%</span></div>
</div>

<figure>
<img src="/img/example-backtest.svg" alt="Chart of AAA's closing price and 10-day average from January to March 1990, with seven buys and seven sells, and the portfolio value falling from $10,000 to $9,652.">
<figcaption>AAA drifted down from about $51 to $44. Most moves above the average reversed within days, so six of the seven round trips lost money.</figcaption>
</figure>

The rule lost $348 (3.5%). That is the point of backtesting: you find out on historical data, not with real money. The first fills in the order record:

```text
|      :date | :permno | :price | :aprc | :quantity |
|------------+---------+--------+-------+-----------|
| 1990-01-16 |   10001 |  51.04 | 51.04 |      50.0 |
| 1990-01-23 |   10001 |  50.12 | 50.12 |     -50.0 |
| 1990-02-07 |   10001 |  48.06 | 48.06 |      50.0 |
| 1990-02-08 |   10001 |   47.5 | 47.50 |     -50.0 |
```

## Start learning

New to Clojure or to backtesting? Read these pages in order.

<div class="card-grid">
<a class="card" href="/posts/get-started/"><span class="step">1</span><span class="card-title">Get Started</span><span class="card-text">Install, open a REPL and run your first backtest on the sample data.</span><span class="more">Install and run →</span></a>
<a class="card" href="/posts/concepts/"><span class="step">2</span><span class="card-title">Key Concepts</span><span class="card-text">The daily loop, when orders fill, prices, margin and the performance metrics.</span><span class="more">Learn the ideas →</span></a>
<a class="card" href="/posts/user-guide/"><span class="step">3</span><span class="card-title">Data and Trading Rules</span><span class="card-text">The datasets and their columns, the exact trading rules, and the cache.</span><span class="more">Read the rules →</span></a>
<a class="card" href="/posts/cheat-sheet/"><span class="step">4</span><span class="card-title">Cheat Sheet</span><span class="card-text">Every function on one page, grouped by task, with links to the details.</span><span class="more">Browse functions →</span></a>
<a class="card" href="/posts/examples/"><span class="step">5</span><span class="card-title">Examples</span><span class="card-text">Complete strategies: moving-average crossovers, Bollinger Bands, margin trading and fundamentals.</span><span class="more">See strategies →</span></a>
<a class="card" href="/posts/faq/"><span class="step">?</span><span class="card-title">FAQ and Troubleshooting</span><span class="card-text">Why an order did not fill, why an indicator returns <code>nil</code>, and other common questions.</span><span class="more">Get unstuck →</span></a>
</div>

## Video introduction

A walkthrough of the installation, an example strategy and tips for learning the library. It was recorded with an earlier version of the library, so some details differ from these pages.

<div class="video"><iframe src="https://www.youtube.com/embed/7F4gUWOhkB8" title="Introduction to the Clojure backtester" loading="lazy" allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe></div>
