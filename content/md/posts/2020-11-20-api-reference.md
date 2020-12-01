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
<{{:ewretd "0.002093", :HSICCD "3571", :CFACSHR "56", :date "1980-12-15", :OPENPRC "", :SECSTAT "R", :SHROUT "55.136", :TICKER "AAPL", :COMNAM "APPLE COMPUTER INC", :PRIMEXCH "Q", :TRDSTAT "A", :HEXCD "3", :RET "-0.052061", :EXCHCD "3", :CFACPR "56", :DLRET "", :PRC "27.3125", :vwretd "0.001605", :CUSIP "03783310", :NCUSIP "03783310", :PERMCO "00007", :PERMNO "14593", :SHRCD "11", :sprtrn "0.001702", :VOL "", :SICCD "3573"}...}>
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
<[[:ewretd ["0.002093" "-0.001389" "0.007502" "0.006936" "0.008056" "0.007997" "0.001412" "0.004105" "0.004242" "-0.005709" "0.001174" "0.009927" "0.009458" "0.010594" "0.005368" "-0.03238" "-0.00516" "0.008907" "0.004471" "-0.000664" "0.005702" "0.004183" "0.005738" "0.002124" "-0.010683" "-0.002868" "-0.004657" "0.001699" "-0.003975" "0.004729" "0.001001" "0.002597" "0.001105" "-0.018646" "0.001114" "0.004765" "0.007917" "0.007249" "-0.00282" "-0.001213" "-0.002401" "-0.004518" "-0.001726" "-5.5e-05" "0.001811" "-0.008043" "-0.001957" "0.001809" "0.002916" "-3.4e-05" "0.008656" "0.008212" "0.003819" "-0.001614" "0.002521" "0.001221" "0.002958" "0.003551" "-0.002266" "-0.001807" "0.011681" "0.005793" "0.006435" "-0.000237" "0.004204" "0.003192" "0.008718" "0.005984" "-0.000118" "0.009045" "0.001948" "-0.002487" "-0.000217" "0.007541" "0.005162" "0.002897" "0.002749" "-0.006616" "0.001807" "0.003888" "0.005053" "0.004563" "-0.005292" "-0.003674" "0.006399" "0.008239" "0.003353" "-0.002742" "0.000864" "0.002988" "0.006598" "0.001707" "-0.00814" "-0.005957" "0.00279" "-0.000297" "-0.013762" "-0.006381" "0.002456" "0.005405" "0.004743" "-0.007511" "0.000823" "0.002881" "0.005549" "0.005913" "0.001988" "-0.002401" "0.00334" "0.001324" "0.003778" "0.00442" "0.006344" "0.004271" "0.003653" "0.000907" "-0.011098" "-0.003048" "0.00273" "0.004895" "-8.9e-05" "-0.003319" "0.001876" "0.00827" "0.003869" "0.001707" "-0.008553" "0.000689" "-0.004752" "0.00258" "-0.001013" "0.003547" "-0.001183" "0.001235" "0.000481" "-0.005352" "-0.007092" "-0.006769" "-0.006941" "-0.0139" "-0.004077" "-0.000875" "0.006533" "0.004753" "0.001068" "-0.004419" "0.004426" "0.003234" "0.00429" "-0.012409" "-0.008818" "-0.00373" "-0.002009" "0.006128" "0.004585" "-0.002349" "0.001375" "0.004231" "0.006598"...]...]>
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

The file we used in this demonstration can be found [here].
[here]: https://github.com/clojure-finance/clojure-backtesting/blob/master/resources/plotting-testing-data.csv

```
;; define dataset:
(def multistocks (read-csv-row "../resources/plotting-testing-data.csv"))

;; dataset sample:
(first multistocks)
;;output: 
<{:date "1980-12-15", :tic "AAPL", :price "27.3125", :return "-0.052061"}>

;;plotting the data in single y-axis
(plot multistocks :tic :date :price)
;; output:
![alt text](https://github.com/clojure-finance/clojure-backtesting-website/blob/master/content/img/Multistocks%20Plot%20Single%20Axis.png "Singel Axis Plot")


;;plotting the data in dual y-axis
(plot multistocks :tic :date :price :return)

;; output:
![alt text](https://github.com/clojure-finance/clojure-backtesting-website/blob/master/content/img/Multistocks%20Plot%20Dual%20Axis.png "Dual Axis Plot")
```
