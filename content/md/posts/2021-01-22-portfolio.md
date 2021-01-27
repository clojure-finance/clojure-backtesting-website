{:title "Portfolio"
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

### Portfolio Manipulation
Functions for creating and viewing the portfolio; and for inspecting historical records of portfolio values.

---


`init_portfolio`

This function initialises the portfolio with cash and a date. Note that is is a **must** to call the function before executing functions e.g. `available-tics` and those in the `counter` namespace.

**Parameters:**
- date - the starting date of the portfolio, in format "YYYY-MM-DD"
- init-capital - the desired initial capital for the portfolio (non-negative integer)

**Example**:

```
(init_portfolio "1980-12-1" 1000000)

;; output:
null
```

<br>

`view_portfolio`

This function prints the portfolio in a table format. 

**Parameters:**
- none

**Ouput explanation:**

| &nbsp;Column&emsp;| &nbsp;Format &emsp;| &nbsp;Meaning  |
| ------------ | :-----------: | :----------|
| `asset`       | &nbsp;string | &nbsp;Cash or ticker of the stock         |
| `price`     | &nbsp;float, $ | &nbsp;Price of the stock &emsp; |
| `aprc`     | &nbsp;float, % | &nbsp;Adjusted price of the stock &emsp; |
| `quantity`     | &nbsp;int or float | &nbsp;Quantity of the stock owned &emsp; |
| `tot_val`     | &nbsp;int, $ | &nbsp;Total value of the stock &emsp; |

<br>

**Example**:

```
(view_portfolio)

;; output:
| :asset |  :price | :aprc | :quantity | :tot_val |
|--------+---------+-------+-----------+----------|
|   cash |     N/A |   N/A |       N/A |    10295 |
|   AAPL |   34.19 | 29.42 |         0 |        0 |
```

<br>

`view_portfolio_record`

This function prints the historical values and daily returns of the portfolio in a table format. 

**Parameters:**
- none

**Ouput explanation:**

| &nbsp;Column&emsp;| &nbsp;Format &emsp;| &nbsp;Meaning  |
| ------------ | :-----------: | :----------|
| `date`       | &nbsp;YYYY-MM-DD | &nbsp;Date of record            |
| `tot_value`     | &nbsp;int, $ | &nbsp;Total value of the portfolio &emsp; |
| `daily_ret`     | &nbsp;float, % | &nbsp;Daily return of the portfolio &emsp; |
| `tot_ret`       | &nbsp;float, % | &nbsp;Total return of the portfolio &emsp; |
| `loan`          | &nbsp;int, $ | &nbsp;Amount of loan made (cumulative) &emsp; |
| `leverage` | &nbsp;float, % | &nbsp;Leverage ratio given by (total debt / total equity) &emsp; |

<br>

**Example**:

```
(view_portfolio_record)

;; output:
|      :date | :tot_value | :daily_ret |
|------------+------------+------------|
| 1980-12-16 |     $10000 |      0.00% |
| 1980-12-17 |     $10007 |      0.00% |
| 1980-12-18 |     $10026 |      0.00% |
| 1980-12-22 |     $10096 |      0.01% |
| 1980-12-24 |     $10168 |      0.01% |
| 1980-12-29 |     $10254 |      0.01% |
| 1980-12-31 |     $10295 |      0.00% |
```

---


### Evaluation Metrics
Functions for computing and printing the summary statistics that evaluate the performance of the portfolio. 

<br>

`update-eval-report`

This function updates the evaluation metrics in the record. (see `eval-report` for the list of evaluation metrics computed)

**Parameters:**
- date - date to update the evaluation metrics, in format "YYYY-MM-DD"


**Example**:
```
;; update the evaluation metrics today
(update-eval-report (get-date)) 

;; output:
null
```


<br>

`eval-report`

This function prints the evaluation report that includes all summary statiscs in a table format.

**Parameters:**
- none

**Ouput explanation:**

| &nbsp;Column&emsp;| &nbsp;Format &emsp;| &nbsp;Meaning  |
| ------------ | :-----------: | :----------|
| `date`       | &nbsp;YYYY-MM-DD | &nbsp;Date of record            |
| `pnl-pt`     | &nbsp;float, $ | &nbsp;Profit and loss per trade &emsp; |
| `sharpe`     | &nbsp;float, % | &nbsp;Sharpe ratio of the portfolio caculated with an expanding window &emsp; |
| `tot-val`    | &nbsp;int, %   | &nbsp;Total value of the portfolio, including cash and all purchased stocks &emsp; |
| `vol`        | &nbsp;float, % | &nbsp;Volatility of the portfolio caculated with an expanding window &emsp; |

<br>

**Example**:
```
(eval-report)

;; output:

|      :date | :pnl-pt | :sharpe | :tot-val |  :vol |
|------------+---------+---------+----------+-------|
| 1980-12-18 |   $6714 |   1.41% |  $106714 | 4.60% |
| 1981-12-21 |   $2166 |  15.72% |  $106499 | 0.40% |
| 1982-12-20 |   $1702 |  23.22% |  $108511 | 0.35% |
| 1983-12-19 |     $95 |   1.80% |  $100571 | 0.32% |
```

<br>

`portfolio-total`

This function returns the current total value of the portfolio.

**Parameters:**
- none

Example & output:

```
(portfolio-total)

;; output:
<type output here>
```

<br>

`portfolio-daily-ret`

This function returns the current daily return of the portfolio.

**Parameters:**
- none

Example:

```
(portfolio-daily-ret)

;; output:
<type output here>
```

<br>

`portfolio-total-ret`

This function returns the total daily return of the portfolio.

**Parameters:**
- none

**Example**:

```
(portfolio-total-ret)

;; output:
<type output here>
```



---

### Plot Graphs
Functions for generating line plots on chosen variables.

<br>

`plot`

This function allows users to plot line charts, single y-axis or dual y-axis. This plotting function works the best with Clojupyter Notebook. For Clojupyter tutorials, please refer to [this page]. For Clojupyter Notebook examples, please refer to [here].

[this page]: https://github.com/clojure-finance/clojure-backtesting/tree/master/clojupyter
[here]: https://github.com/clojure-finance/clojure-backtesting/tree/master/examples

**Parameters:**
- dataset - data to be plotted. The dataset should be in following format: `{:tic "AAPL" :date "1980-12-15" :price "27.00" :return "-0.5 }`
- series - series name of the lines, e.g. :tic, which means ticker name
- y1 - primary y axis, e.g. `:price`, trading price of the stock
- y2 - **optional** secondary y axis, e.g. `:return`, daily return of the stock

<br>

**Example**:

The file we used in this demonstration can be found [in this link].

[in this link]: https://github.com/clojure-finance/clojure-backtesting/blob/master/resources/plotting-testing-data.csv

```
;; define dataset:
(def multistocks (read-csv-row "../resources/plotting-testing-data.csv"))

;; dataset sample:
(first multistocks)
;; output: 
<{:date "1980-12-15", :tic "AAPL", :price "27.3125", :return "-0.052061"}>
```

```
;; plotting the data in single y-axis
(plot multistocks :tic :date :price)
;; output:
```
![alt text](https://github.com/clojure-finance/clojure-backtesting-website/blob/master/content/img/Multistocks%20Plot%20Single%20Axis.png "Singel Axis Plot")

```
;; plotting the data in dual y-axis
(plot multistocks :tic :date :price :return)
;; output:
```
![alt text](https://github.com/clojure-finance/clojure-backtesting-website/blob/master/content/img/Multistocks%20Plot%20Dual%20Axis.png "Dual Axis Plot")
