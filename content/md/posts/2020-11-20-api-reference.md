{:title "API reference"
 :layout :post
 :tags  []
 :toc true}

---
# Data Handling
<br>

## Read Data

1. Read Data By Row

`read-csv-row`

Read the CSV file into memeory in a row by row format. 

**Parameters:**
- file - the csv file to be loaded

<br>

Example & output:

```
# (read-csv-row "./resources/CRSP-extract.csv")

{{example photo}}


```

<br>

2. Read Data By Column

`read-csv-col`

Read the CSV file into memeory in a column by column format. 

**Parameters:**
- file - the csv file to be loaded

<br>

Example & output:

```
# (read-csv-col "./resources/CRSP-extract.csv")

{{example photo}}


```

<br>

## Merge Data

1. Merge Data By Row

`read-csv-row`

Merge 2 CSV files row by row using left-join method, i.e. merging the trading data with the accounting data. Merge data by row is suggested. 

**Parameters:**
- file1 - the CRSP trading dataset
- file2 - the COMPUSTAT accounting dataset

<br>

Example & output:

```
# (merge-data-row "./resources/CRSP-extract.csv" "./resources/Compustat-extract.csv")

{{example photo}}


```

<br>

2. Merge Data By Column

`merge-csv-col`

Merge 2 CSV files column by column using left-join method, i.e. merging the trading data with the accounting data. 

**Parameters:**
- file1 - the CRSP trading dataset
- file2 - the COMPUSTAT accounting dataset

<br>

Example & output:

```
# (merge-data-col "./resources/CRSP-extract.csv" "./resources/Compustat-extract.csv")

{{example photo}}


```

# Portfolio

## Create Initial Portfolio

`init_portfolio`

This function creates initial portfolio with cash only, user can input the initial capital they desire. 

**Parameters:**
- date - the start date of the portfolio
- init-capital - the desired initial capital for the portfolio

<br>

Example & output:

```
# (init_portfolio "1980-12-1" 1000000)

{{example photo}}


```

## Portfolio Evaluation

This section describes the portfolio evaluation functions that are available.

### Evaluation Report

`eval-report`

This function prints the evaluation report in a table format.

**Parameters:**
- none

<br>

### View current portfolio
<br>

### View historical portfolio values and returns
<br>

### Generate evaluation report
Example & output:

```
# (eval-report)

{{example photo}}


```

### Portfolio Total

`portfolio-total`

This function returns the current total value of the portfolio.

**Parameters:**
- none

<br>

Example & output:

```
# (portfolio-total)

{{example photo}}


```

### Portfolio Daily Return

`portfolio-daily-ret`

This function returns the current daily return of the portfolio.

**Parameters:**
- none

<br>

Example & output:

```
# (portfolio-daily-ret)

{{example photo}}


```

### Portfolio Daily Total Return

`portfolio-total-ret`

This function returns the total daily return of the portfolio.

**Parameters:**
- none

<br>

Example & output:

```
# (portfolio-total-ret)

{{example photo}}


```


# Oders

## Make an order

`order`

This order function allows user to set orders. 

**Parameters:**
- tic - the ticker of the stock to buy or sell
- quantity - quantity to buy or sell, +ve means buy, -ve means sell
- remaining - ???

<br>

Example & output:

```
# (order AAPL 10)

{{example photo}}


# (order IBM -10)

{{example photo}}

```

# Plotting

## Line Plots

`plot`

This function allows users to plot line charts, single y-axis or dual y-axis. This plotting function works the best with Clojupyter Notebook. For Clojupyter tutorials, please refer to [this page]. For Clojupyter Notebook examples, please refer to [here].

[this page]: https://github.com/clojure-finance/clojure-backtesting/tree/master/clojupyter
[here]: https://github.com/clojure-finance/clojure-backtesting/tree/master/examples

**Parameters:**
- dataset - data to be plotted. The dataset should be in following format: {:tic "AAPL" :date "1980-12-15" :price "27.00" :return "-0.5 }
- series - series name of the lines, e.g. :tic, which means ticker name
- y1 - primary y axis, e.g. :price, trading price of the stock
- y2 - **optional** secondary y axis, e.g. :return, daily return of the stock

<br>

Example & output:

```
# (plot data :tic :date :price)

{{example photo}}


# (plot data :tic :date :price :return)

{{example photo}}

```
