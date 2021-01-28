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
- `date` - the starting date of the portfolio, in format "YYYY-MM-DD"
- `init-capital` - the desired initial capital for the portfolio (non-negative integer)

**Example**:

```clojure
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

```clojure
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

```clojure
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
- `date` - date to update the evaluation metrics, in format "YYYY-MM-DD"


**Example**:
```clojure
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
```clojure
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

```clojure
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

```clojure
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

```clojure
(portfolio-total-ret)

;; output:
<type output here>
```



---

### Plot Graphs
Functions for generating line plots on chosen variables.

<br>

`plot`

This function allows users to plot line charts. This plotting function works the best with the Jupyter Notebook.

**Parameters:**
- dataset - contains a map of data to be plotted. Each map should be in the following format: `{:tic "AAPL" :date "1980-12-15" :price "27.00" :return "-0.5 }`
- `series` - series name of the lines to appear in the legend
- `x` - key that contains that x-axis data in the dataset, e.g. `:date`
- `y` - key that contains that y-axis data in the datset, e.g. `:portfolio-value`
- `full-date` - boolean, set to true if you want to have full date (i.e. month, day, year) as labels in the x-axis; if set as false the function would automatically choose the appropariate labels

**Note**: pass full-date as `true` when plotting variables in the **evaluation report**.

<br>

**Example**:

```clojure
;; data to print: an atom of maps

(first data) ;; print first row
;; output
;; {:date "1980-12-16", :pnl-pt 7.806415917975755, :sharpe 1.4142135623730954, :tot-val 10007.806415917976, :vol 0.05517816194058409}

;; Add legend name to series
(def data-to-plot
 (map #(assoc % :plot "sharpe")
  data))

;; Call plotting function
(plot data-to-plot :plot :date :vol true)
```

<br>

Output:

<br>

![image](/img/plot-sharpe.png)
