{:title "Preliminaries"
 :layout :post
 :tags  []
 :toc true}

---

### Example Dataset Format
<br>



#### Standard Dataset Format

<br>

_Requirements:_

1. In csv file format
2. **Row based:** rows are instances and columns are parameters. The first line is the header for each column.
3. Different tickers are uniquely identified by the Ticker ID or Name. And the same ticker has the same ID or name.
4. Same tickers are stored in clusters or blocks. The row numbers are continuous.
5. Dates within each cluster or block is in order from the earliest to the lastest date.
6. The dataset is less than 100MB.

**Some remarks:**

1. The date in init-portfolio must be a valid date in the dataset.
2. Ticker should have a unique identifier named TICKER, which should not be null

<br>

#### Large Dataset Format

<br>

_Requirements:_

1. In csv file format
2. **Row based:** rows are instances and columns are parameters. The first line is the header for each column.
3. Different tickers are uniquely identified by the Ticker ID or Name. And the same ticker has the same ID or name.
4. Tickers on the same date are stored in clusters or blocks.
5. Dates within each cluster or block is stored by alphabetical order of names.
6. The dataset is more than 100MB.

<br>

**Some remarks:**

1. The date in init-portfolio must be a valid date in the dataset.
2. Ticker should have a unique identifier named TICKER, which should not be null
