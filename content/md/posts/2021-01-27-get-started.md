{:title "Get Started"
:layout :post
:toc true}

---

### Setting Up the Playground

<br>

1. Make sure that you have **leiningen** and **jupyter notebook** installed on your local machine. (These two may require further environment dependencies, please refer to their official websites for details.)

   - Installation guide for leiningen can be found at:

     <https://github.com/technomancy/leiningen/wiki/Packaging>

   - Jupyter notebook can be installed at:

     <https://jupyter.org/install>

<br>

2. If you download this project for the **first time**, go to the repository root directory (i.e. `clojure-backtester/`) in the terminal and run

   `make init_clojupyter`

   _Note that this operation may download a number of required packages, which may take up some time._

<br>

3. Run the following command so that the project could be compiled as a .jar file and added as a new Jupyter kernel:

   `make add_kernel`

   (If there is an error, make sure that you have **terminated** the Jupyter notebook running kernels before running this step.)

   <br>

   When the process is completed, you should see the following output:

   ```
   Installed jar:      target/uberjar/clojure-backtesting-0.1.0-SNAPSHOT-standalone.jar
   Install directory:  ~/Library/Jupyter/kernels/backtesting_clojure
   Kernel identifier:  backtesting_clojure

   Installation successful.
   ```

<br>

4. Now when you restart the Jupyter Notebook application, you could select the kernel named `backtesting_clojure`. You can make use of the backtester by choosing this kernel.

---

### Beginner Tutorials

<br>

If you are new to clojure, we recommend having a quick read of the following tutorials first:

- [Clojure by example](http://kimh.github.io/clojure-by-example/#about) - useful for beginners pick up the syntax quickly

- [Clojure docs](https://clojuredocs.org/) - a more thorough documentation that explains the built-in functions in Clojure

---

### Code walkthrough

We'll go through the code in `./examples/Simple trading strategy.ipynb` notebook to have a glimpse of how to write code that could be run with the backtester.

<br>

**1. Import libraries**

To make use of the functions in the backtester library, it is necessary to import them whenever you create a new jupyter notebook file. Also make sure that you've compiled the **most up-to-date** clojure-backteser kernel, and have selected it in the Jupyter Notebook application.

```
(ns clojure-backtesting.demo
  (:require [clojure.test :refer :all]
            [oz.notebook.clojupyter :as oz]
            [clojure-backtesting.data :refer :all]
            [clojure-backtesting.order :refer :all]
            [clojure-backtesting.evaluate :refer :all]
            [clojure-backtesting.plot :refer :all]
            [clojure-backtesting.counter :refer :all]
            [clojure-backtesting.parameters :refer :all]
            [clojure.string :as str]
            [clojure.pprint :as pprint]
            [java-time :as t]
            [clojupyter.kernel.version :as ver]
            [clojupyter.misc.helper :as helper]
  ) ;; require all libriaries from core
  (:use clojure.pprint)
)
```

<br>

**2. Import dataset**

Load the CRSP-extract.csv dataset to the program by providing its relative path. Note that it is a must to nest it with `read-csv-row` and `add-aprc`, as they respectively parse the csv file and automatically add the adjusted price column to the dataset.

```
(reset! data-set (add-aprc (read-csv-row "../resources/CRSP-extract.csv")));
```

<br>

**3. Initialise portfolio**

Pass the date ("YYYY-MM-DD") and initial capital (non-negative integer) to the `init-portfolio` function.

```
(init-portfolio "1980-12-16" 10000);
```

<br>

**4. Check available tickers**

You could check what tickers you could trade on the current date (i.e. 1980-12-16).

```
(keys (deref available-tics))
```

<br>

**5. Write your strategy**

With all these set-up, you are ready to write your strategy.

<br>

The following demo is a trading strategy that follows a simple logic.

**Simple strategy**:

In a timespan of 10 days (inclusive of today),

- Buy 50 stocks of AAPL on the first day
- Sell 10 stocks of AAPL on every other day

```
;; define the "time span", i.e. to trade in the coming 10 days
(def num-of-days (atom 10))

(while (pos? @num-of-days) ;; check if num-of-days is > 0
    (do
        ;; write your trading strategy here
        (if (= 10 @num-of-days) ;; check if num-of-days == 10
            (do
                (order "AAPL" 50) ; buy 50 stocks
                (println ((fn [date] (str "Buy 50 stocks of AAPL on " date)) (get-date)))
            )
        )
        (if (odd? @num-of-days) ;; check if num-of-days is odd
            (do
                (order "AAPL" -10) ; sell 10 stocks
                (println ((fn [date] (str "Sell 10 stocks of AAPL on " date)) (get-date)))
            )
        )

        (update-eval-report (get-date)) ;; update the evaluation metrics every day

        ; move on to the next trading day
        (next-date)

        ; decrement counter
        (swap! num-of-days dec)
    )
)

; call this so as to create output files
(end-order)

; check whether counter == 0
(println ((fn [counter] (str "Counter: " counter)) @num-of-days))
```

Note that in the above code, it is necessary to iteratively call `next-date` so that the system could "move on to the next trading day". (check the details in the _"Counter"_ section)

<br>

```clojure
;; output:

Buy 50 stocks of AAPL on 1980-12-16
Sell 10 stocks of AAPL on 1980-12-17
Sell 10 stocks of AAPL on 1980-12-19
Sell 10 stocks of AAPL on 1980-12-23
Sell 10 stocks of AAPL on 1980-12-26
Sell 10 stocks of AAPL on 1980-12-30
Counter: 0
```

By printing these debugging messages, I could double-check whether the orders were accurate.

<br>

**6. Check order record**

Alternatively, you could also directly view the order record.

```clojure
(pprint/print-table (deref order-record))
```

```clojure
;; output:

|      :date | :tic |  :price | :quantity | :reference |
|------------+------+---------+-----------+------------|
| 1980-12-17 | AAPL | 25.9375 |        50 |          1 |
| 1980-12-18 | AAPL | 26.6875 |       -10 |          1 |
| 1980-12-22 | AAPL | 29.6875 |       -10 |          1 |
| 1980-12-24 | AAPL | 32.5625 |       -10 |          1 |
| 1980-12-29 | AAPL | 36.0625 |       -10 |          1 |
| 1980-12-31 | AAPL | 34.1875 |       -10 |          1 |
```

<br>

**7. View portfolio & portfolio record**

You could view the portfolio and check the changes in portfolio value too. Note that the portfolio record could also be found in the `out_portfolio_value_record.csv` file.

```clojure
(view-portfolio)
```

```clojure
;; output:
| :asset |  :price | :aprc | :quantity | :tot-val |
|--------+---------+-------+-----------+----------|
|   cash |     N/A |   N/A |       N/A |    10295 |
|   AAPL | 34.1875 | 29.42 |         0 |        0 |
```

<br>

```clojure
;; pass -1 so that the entire record is printed, else pass the no. of rows
(view-portfolio-record -1)
```

```clojure
;; output:
|      :date | :tot-value | :daily-ret | :tot-ret | :loan | :leverage |
|------------+------------+------------+----------+-------+-----------|
| 1980-12-16 |     $10000 |      0.00% |    0.00% | $0.00 |     0.00% |
| 1980-12-17 |     $10000 |      0.00% |    0.00% | $0.00 |     0.00% |
| 1980-12-18 |     $10016 |      0.00% |    0.16% | $0.00 |     0.00% |
| 1980-12-19 |     $10043 |      0.27% |    0.44% | $0.00 |     0.00% |
| 1980-12-22 |     $10066 |      0.00% |    0.66% | $0.00 |     0.00% |
| 1980-12-23 |     $10081 |      0.15% |    0.81% | $0.00 |     0.00% |
| 1980-12-24 |     $10100 |      0.00% |    1.00% | $0.00 |     0.00% |
| 1980-12-26 |     $10122 |      0.22% |    1.22% | $0.00 |     0.00% |
| 1980-12-29 |     $10126 |      0.00% |    1.26% | $0.00 |     0.00% |
| 1980-12-30 |     $10123 |     -0.03% |    1.22% | $0.00 |     0.00% |
| 1980-12-31 |     $10119 |      0.00% |    1.19% | $0.00 |     0.00% |
```

<br>

**8. Generate evaluation report**

If you update the evaluation report every day (as `update-eval-report` is called for 10 times in the loop), you'll obtain a evluation report with daily records.

Detailed explanation of the evaluation metrics could be found in the _"Portfolio"_ section.

However, note that if you are traversing a large amount of dates, it would be better **not** to update the evaluation metrics every day as it would require a large amount of memory and computation time, or to only print a small number of rows of the evaluation report.

 Note that the evaluation report could also be found in the `out_evaluation_report.csv` file.

<br>

```clojure
;; pass -1 so that the entire record is printed, else pass the no. of rows
(eval-report -1)
```

```Clojure
;; output:
|      :date | :pnl-pt | :sharpe | :tot-val |  :vol |
|------------+---------+---------+----------+-------|
| 1980-12-16 |      $0 |   0.00% |   $10000 | 0.00% |
| 1980-12-17 |      $8 |   1.73% |   $10016 | 0.09% |
| 1980-12-18 |      $8 |   0.00% |   $10016 | 0.00% |
| 1980-12-19 |     $22 |   4.80% |   $10066 | 0.14% |
| 1980-12-22 |     $22 |   5.39% |   $10066 | 0.12% |
| 1980-12-23 |     $25 |   8.68% |   $10100 | 0.11% |
| 1980-12-24 |     $25 |   9.13% |   $10100 | 0.11% |
| 1980-12-26 |     $25 |  11.44% |   $10126 | 0.11% |
| 1980-12-29 |     $25 |  11.21% |   $10126 | 0.11% |
| 1980-12-30 |     $19 |  10.90% |   $10119 | 0.11% |

```

<br>

**9. Plot variables**

You could try plotting some variables shown in the portfolio record / evaluation report tables.

**Plotting values in portfolio record**

```clojure
;; 1) Define the data to be the record that features the column
(def data (deref portfolio-value))

;; 2) Assign it to a new variable 'data-to-plot`, so that
;; we could have the legend name added (here = "port-value")
(def data-to-plot
 (map #(assoc % :plot "port-value")
  data))

;; 3) Pass the data and its keys to the plotting function
;; last arg "false" means we do not need full date to be shown on the x-axis
(plot data-to-plot :plot :date :daily-ret false)
```

<br>

Output:

<br>

![image](/img/plot-portfolio-value.png)

<br>

**Plotting values in evaluation report**

```clojure
(def data (deref eval-record))
(def data-to-plot
 (map #(assoc % :plot "volatility")
  data))

;; Note that we need to set it as "true" for plotting values in eval-report
(plot data-to-plot :plot :date :vol true)
```

<br>

Output:

<br>

![image](/img/plot-volatility.png)
