{:title "API reference"
 :layout :post
 :tags  []
 :toc true}

---
<style>
/* table styles */
table, th, td {
  border: 1px solid black;
  padding: 5px;
}
</style>

### Read Data
Functions for reading csv data.
<br>
<br>

`read-csv-row`

Read the CSV file into memeory in a row by row format. 

**Parameters:**
- file - path to the csv file to be read

<br>

Example:

```
(read-csv-row "./resources/CRSP-extract.csv")

;; output:
<type output here>
```

<br>


`read-csv-col`

Read the CSV file into memeory in a column by column format. 

**Parameters:**
- file - path to the csv file to be read

Example:

```
(read-csv-col "./resources/CRSP-extract.csv")

;; output:
<type output here>
```

---


### Merge Data
Functions for merging the csv files either by row or by column.

<br>

`merge-data-row`

Merge 2 CSV files row by row using left-join method, i.e. merging the trading data with the accounting data. Merging that data by row is recommended. 

**Parameters:**
- file1 - the CRSP trading dataset
- file2 - the COMPUSTAT accounting dataset

**Example**:

```
(merge-data-row "./resources/CRSP-extract.csv" "./resources/Compustat-extract.csv")

;; output:
<type output here>
```
<br>


`merge-csv-col`

Merge 2 CSV files column by column using left-join method, i.e. merging the trading data with the accounting data. 

**Parameters:**
- file1 - the CRSP trading dataset
- file2 - the COMPUSTAT accounting dataset

**Example**:

```
# (merge-data-col "./resources/CRSP-extract.csv" "./resources/Compustat-extract.csv")

;; output:
<type output here>
```

---

### Portfolio
Functions for creating and viewing the portfolio; and for inspecting historical records of portfolio values.

<br>

`init_portfolio`

This function initialises the portfolio with cash only, user can input the initial capital they desire. 

**Parameters:**
- date - the starting date of the portfolio
- init-capital - the desired initial capital for the portfolio

**Example**:

```
(init_portfolio "1980-12-1" 1000000)

;; output:
<type output here>
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
|   AAPL | 34.1875 | 29.42 |         0 |        0 |
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

### Summary Statistics
Functions for computing and printing the summary statistics that evaluate the performance of the portfolio. 

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
| `ret-da`     | &nbsp;float, % | &nbsp;Daily return &emsp; |
| `ret-r`      | &nbsp;float, % | &nbsp;Return of the portfolio calculated with a rolling fixed window of 1 year &emsp; |
| `ret-tot`    | &nbsp;float, % | &nbsp;Total return of the portfolio, i.e. cumulative daily returns &emsp; |
| `sharpe-e`   | &nbsp;float, % | &nbsp;Sharpe ratio of the portfolio caculated with an expanding window &emsp; |
| `sharpe-r`   | &nbsp;float, % | &nbsp;Sharpe ratio of the portfolio calculated with a rolling fixed window of 1 year &emsp; |
| `tot-val`    | &nbsp;int, %   | &nbsp;Total value of the portfolio, including cash and all purchased stocks &emsp; |
| `vol-e`      | &nbsp;float, % | &nbsp;Volatility of the portfolio caculated with an expanding window &emsp; |
| `vol-r`      | &nbsp;float, % | &nbsp;Volatility of the portfolio calculated with a rolling fixed window of 1 year &emsp; |

<br>

**Example**:
```
(eval-report)

;; output:

|      :date | :pnl-pt | :ret-da | :ret-r | :ret-tot | :sharpe-e | :sharpe-r | :tot-val | :vol-e | :vol-r |
|------------+---------+---------+--------+----------+-----------+-----------+----------+--------+--------|
| 1980-12-16 |   $7.81 |   0.08% |  0.22% |    0.00% |     1.41% |    24.80% |   $10007 |  0.06% |  0.88% |
| 1980-12-17 |  $13.37 |   0.19% |  0.96% |    0.00% |     2.81% |    63.55% |   $10026 |  0.10% |  1.51% |
| 1980-12-18 |  $13.37 |   0.19% |  0.96% |    0.00% |     2.81% |    63.55% |   $10026 |  0.10% |  1.51% |
| 1980-12-19 |  $32.32 |   0.70% |  0.62% |    0.01% |     3.07% |    12.48% |   $10096 |  0.31% |  4.99% |
| 1980-12-22 |  $32.32 |   0.70% |  0.62% |    0.01% |     3.07% |    12.48% |   $10096 |  0.31% |  4.99% |
| 1980-12-23 |  $42.14 |   0.71% |  0.82% |    0.02% |     4.88% |    15.01% |   $10168 |  0.34% |  5.44% |
| 1980-12-24 |  $42.14 |   0.71% |  0.82% |    0.02% |     4.88% |    15.01% |   $10168 |  0.34% |  5.44% |
| 1980-12-26 |  $50.84 |   0.84% |  0.68% |    0.03% |     6.80% |    11.66% |   $10254 |  0.37% |  5.86% |
| 1980-12-29 |  $50.84 |   0.84% |  0.68% |    0.03% |     6.80% |    11.66% |   $10254 |  0.37% |  5.86% |
| 1980-12-30 |  $49.17 |   0.40% |  0.68% |    0.03% |     8.62% |    12.62% |   $10295 |  0.34% |  5.35% |
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

### Make Order
Functions for making an order to trade a stock.

<br>

`order`

This order function allows user to set orders. 

**Parameters:**
- tic - the ticker of the stock to buy or sell
- quantity - quantity to buy or sell, +ve means buy, -ve means sell
- remaining - ???

Example:

```
(order AAPL 10)

;; output:
<type output here>


(order IBM -10)

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

```
(plot data :tic :date :price)
;; output:
<type output here>

(plot data :tic :date :price :return)

;; output:
<type output here>
```
