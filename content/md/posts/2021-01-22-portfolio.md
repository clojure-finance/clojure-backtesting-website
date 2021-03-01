{:title "Portfolio"
:layout :post
:tags []
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

| &nbsp;Column&emsp; | &nbsp;Format &emsp; | &nbsp;Meaning                            |
| ------------------ | :-----------------: | :--------------------------------------- |
| `asset`            |    &nbsp;string     | &nbsp;Cash or ticker of the stock        |
| `price`            |   &nbsp;float, $    | &nbsp;Price of the stock &emsp;          |
| `aprc`             |   &nbsp;float, %    | &nbsp;Adjusted price of the stock &emsp; |
| `quantity`         | &nbsp;int or float  | &nbsp;Quantity of the stock owned &emsp; |
| `tot_val`          |    &nbsp;int, $     | &nbsp;Total value of the stock &emsp;    |

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

- `n` - no. of rows to print, if n <= 0, print entire record

**Ouput explanation:**

| &nbsp;Column&emsp; | &nbsp;Format &emsp; | &nbsp;Meaning                                                    |
| ------------------ | :-----------------: | :--------------------------------------------------------------- |
| `date`             |  &nbsp;YYYY-MM-DD   | &nbsp;Date of record                                             |
| `tot_value`        |    &nbsp;int, $     | &nbsp;Total value of the portfolio &emsp;                        |
| `daily_ret`        |   &nbsp;float, %    | &nbsp;Daily return of the portfolio &emsp;                       |
| `tot_ret`          |   &nbsp;float, %    | &nbsp;Total return of the portfolio &emsp;                       |
| `loan`             |    &nbsp;int, $     | &nbsp;Amount of loan made (cumulative) &emsp;                    |
| `leverage`         |   &nbsp;float, %    | &nbsp;Leverage ratio given by (total debt / total equity) &emsp; |

<br>

**Example**:

```clojure
(view_portfolio_record -1)

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
