{:title "Example - Fundamental Analysis"
 :layout :post
 :tags  []
 :toc true}

<br>

`ns: large-data`

This namespace features functions to handle large datasets that cannot be loaded to memory at one time, e.g. data-CRSP.csv.

---

### Read in (to do)

Lazily read in the large datasets, with unique name and function.

<br>

`init-portfolio`

**Parameters:**

- `date` - starting date: **Must be a valid date in the dataset**
- `cash` - the amount of money you have initially

*Optional parameters:* 

- `:standard`: boolean: set to false if you are not using a standard dataset. By default, true.

**Possible usages:**

```clojure
(init-portfolio "1980-12-15" 100000)
;; means that you start at "1980-12-15" havin 100000 cash
```



### Make Order (to do)

Functions for making an order to trade a stock.

<br>

`order-lazy`

This order function allows user to set orders for large dataset. 

**Parameters:**

- `tic` - name / identifier of the ticker to buy or sell (string)
- `quantity` - the quantity to trade. If it is positive, buy the amount, else if it negative, sell the amount. (int/double)

*Optional parameters:* 

- `:remaining` -  boolean: if the quantity given is the trading amount or the exact remaining amount. *By default, false.* It is true, meaning that after this order, I want to have `quantity` amount of certain tickers on my account.

- `:leverage` - boolean: if leverage is **allowed** (if necessary) in this order. Leverage includes buying on margin and short sell. You will only buy on margin if you run out of cash. We will also short sell only when you do not have enough stock. *By default, true.*

- `:dataset` -  lazy sequence: Specify the dataset to look for the order. Note that the function will search from the first line of the given dataset. So if used properly, it could speed up the program. If you are using a standard format dataset, you do not need to use this argument. 

- `:print`  - boolean: If to print the successful orders. By default, false.

- `:direct` - boolean: If to direct the order record into a outer file. By default, true. You will find the file named **out-order-record.csv** in the same directory of your running program, i.e. if you create another clj file, it will be under the same layer of your clj file.

  **The only difference with ordinary `order`:**

- `:expiration` -  int: how many days will this order request be effective. For example, if input 1, then this order request will be withdrawn after tomorrow (if this order does not trade that day because of no such ticker).

<br>

**Possible usages:**

1. *Order with or without leverage:*

   ```clojure
   (order "AAPL" 10) ;with leverage, exact value trading
   (order "AAPL" 10 :leverage false) ;without leverage, exact value trade
   (order "AAPL" -10 :remaining true) ;with leverage, remaining value
   (order "AAPL" -10 :leverage false :remaining true) ;without leverage, remaining value (This must be a failed trade)
   ```

2. *Use together with the available-tics: (This will buy 10 stocks from the first ticker that shows in available-tics.)*

   ```clojure
   (let [tics (deref available-tics) tic (name (first (keys (tics))))]
      (order tic 10 :dataset (get (deref (get (get (dref available-tics) :AAPL) :pointer)) :reference))) ; The part after the dataset is copied from usages of available-tics (but you do not need to do this in real life, it is already built into the function).
   ```

3. *Print the order log:*

   ```clojure
   (order "AAPL" 10 :print true)
   ```

   ​      

---

### Get Corresponding line

<br>

`get-compustat-line`

This function reach out to compustat to find the corresponding line for CRSP.

**Parameters:**

- `line` - map: a line of CRSP dataset (with valid date).
- `name` - the name of compustat dataset


**Example:**

```clojure
(get-compustat-line (get (get (deref available-tics) "IBM") :reference) "compustat")

;; output
{:fincfq "", :dpcy "", :fic "USA", :itccy "", :cshoq "", :dltisq "", :wcapchq "", :niq "59.213", :dpcq "", :sstkq "", :cik "51143.0", :oancfq "", :loq "", :pstkrq "", :oibdpq "", :ipodate "", :datafqtr "1962.5", :prstkcq "", :ivncfy "", :pstkq "", :saleq "467.7", :ltq "", :lctq "", :txdbq "", :dlcq "", :ppentq "", :dpq "", :atq "", :dvpq "", :wcapchy "", :capxy "", :itccq "", :xintq "", :datacqtr "1962.5", :dltrq "", :ibq "59.213", :actq "", :mibtq "", :xrdq "", :sstky "", :invtq "", :dvy "", :oiadpq "", :mibq "", :icaptq "", :txditcq "", :prccq "353.1442", :ceqq "", :sppey "", :dvq "", :seqq "", :fincfy "", :capxq "", :revtq "467.7", :oancfy "", :ivncfq "", :cusip "459200101", :dltisy "", :gvkey "6066", :addzip "10504", :dlttq "", :prstkcy "", :rdq "", :sic "7370.0", :xsgaq "", :exchg "11.0", :dltry "", :rectq "", :sppeq "", :cogsq "", :tic "IBM", :cheq "", :datadate "1962-09-30", :conm "INTL BUSINESS MACHINES CORP"}
```


