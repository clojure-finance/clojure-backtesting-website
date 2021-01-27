{:title "Preliminaries"
 :layout :post
 :tags  []
 :toc true}
 
---

### Example Dataset Format
<br>



### Standard Dataset Format
<br>

_Requirements:_

1. In csv file format
2. **Row based:** rows are instances and columns are parameters. The first line is the header for each column.
3. Different tickers are uniquely identified by the Ticker ID or Name. And the same ticker has the same ID or name.
4. Same tickers are stored in clusters or blocks. The row numbers are continuous.
5. Dates within each cluster or block is in order from the earliest to the lastest date.

<br>

**Some remarks:**

1. The date in init-portfolio must be a valid date in the dataset.

