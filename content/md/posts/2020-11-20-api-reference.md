{:title "API reference"
 :layout :post
 :tags  []
 :toc true}

---
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

<br>

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

---

### Summary Statistics
Functions for computing and printing the summary statistics that evaluate the performance of the portfolio. 

<br>

`eval-report`

This function prints the evaluation report that includes all summary statiscs in a table format.

**Parameters:**
- none

**Example**:
```
(eval-report)

;; output:
<type output here>
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
