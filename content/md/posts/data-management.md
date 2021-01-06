### ns: clojure-backtesting.data-management

#### Global Variables:

null

#### Global functions:

1. get-set

   Return a set of ticker and date combinations.

   1. **Input**: A row based dataset.

   2. **Output**: A set of maps

      Example: #{{:tic "AAPL" :datadate "2020-12-10"}}

2. last-quar

   Return the last quarter of the given date of row. For instance, if the date of row is "2020-12-09", the returned date will be "2020-9-31".

   1. **Input**: one row of the CRSP dataset

   2. **Output**: A map

      Example: {:tic "AAPL" :datadate""}

3. look-n-days-ago

   Return the date n days before the given date

   1. **Input**: 

      `date`: String type: "yyyy-mm-dd"

      `n`: int>=0

   2. **Output**: String date: "yyyy-mm-dd"

4. get-prev-n-days

   Return a vector of map containing the required information for a given period **from the current date (refer to ns:counter for the counting mechanism of the system)**

   1. #### **Input**: 

      `key`: keyword type: the name of the column

      `n`: int: the period length

      `tic`: string: the ticker name

      ***Optional:***

      `pre`: vetor: the previous result

      `reference`: lazy: which line of dataset to refer to first

      ***Remarks:***

      1. Use `pre` or `reference` only if the dataset is subject to the 'standard format' (refer to Dataset).

      2. The logic of the function when `pre` is given is that if the length of the `pre` vector is less than `n`. The function will append one more record to the vector if matched. When the length is equal to `n`, the function will remove the first element of the `pre` and append a new matching result to the tail. And the function will only search in the context of `reference`, so do make sure that the reference given to it is correct.

   2. Output: Vector of map

      Example: [{:date "2020-11-10" :PRC "12"} {:date "2020-11-11" :PRC "19"}]

   3. Possible Usages: 

      Calculating moving average with previous result.

      ```clojure
      ;;See Golden cross Jupyter Notebook.
      ```

      