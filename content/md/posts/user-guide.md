{:title "User Guide"
:date "2021-01-20"
:layout :post
:navbar? true
:page-index 2
:tags []
:toc true}

## About the dataset

**[CRSP](https://connecthkuhk-my.sharepoint.com/:u:/g/personal/u35lyc_connect_hku_hk/ESQNElD-vL1KliKmOf57D2ABLCLYLHoc9wkJWhXUSTzCNw?e=DNaVWm)** is the official dataset for the backtester. New users are recommended to directly use this dataset to try out the functionalities of the backtester. Definitions for each column in CRSP can be found [here](/posts/docs/data_descriptions_guide_0.pdf).

Of course, you can use your own dataset in the backtester. Follow this [guide]() on how to reform arbitrary dataset to be compatible with the backtester.

To load the CRSP dataset, simply run

```clojure
(load-dataset "/Volumes/T7/CRSP" "main" add-aprc) ;; change the first argument to your local path
(load-dataset "/Volumes/T7/Compustat/" "compustat")
```

<br>

**[Compustat](https://connecthkuhk-my.sharepoint.com/:u:/g/personal/u35lyc_connect_hku_hk/EbHsTj2Nrx5Or8PP_GkiCeIBlcrijdG_Bm6JElMaHTTFFA?e=fdECXp)** is a quarterly supplementary file of CRSP. It contains company related information that can be used for further analysis. Definitions for each column in Compustat can be found [here](/posts/docs/FundamentalsAnnual.pdf).

To load the Compustat dataset, simply run

```clojure
(load-dataset "/Volumes/T7/Compustat/" "compustat") ;; change the first argument to your local path
```

The Compustat dataset will not overwrite the CRSP dataset. Instead, the rows of Compustat will be automatically merged with the corresponding CRSP rows. You can still use functions such as `get-info`, `get-info-map`, `get-tic-info` as normal, except that some (not all) rows may be longer, because of Compustat.

Both datasets are available [here](https://connecthkuhk-my.sharepoint.com/:f:/g/personal/u35lyc_connect_hku_hk/EuAAauw1fuNFoDRav2D0J_EB6T_bbfkyNZ5ShN8Xw0cqhw?e=RtOcFL).

### Essential Column Names

##### CRSP dataset

- `:PRC` : closing price

- `:OPENPRC` : opening price

- `:DATE` : date (yyyy-MM-dd)

- `:PERMNO` : security identifier

  **This documentation will use *permno* and *security* interchangeably.**

- `:APRC` : adjusted close price

**Remark:**

- The trading price is by default set to be **closing price**.

## Trading Logic

As introduced in the [time pointer](/posts/api#move-time-pointer), there is a global clock in the backtester. Your strategy will normally be run between two `next-date`s. Below is a figure illustrating the logic of trading with an example.

![image](/img/trade-logic.jpg)

Say you are on date 2022-10-24, and you buy 10 shares of xx security. This order will not be traded immediately. Instead, it will result in a pending order which will be settle by the backtester on the next call of `next-date` (if there is indeed xx security in the market). What you will observe is that your portfolio will not change immediately after `order`, and `order-record` will not be updated either. They will only be updated on the next day. The price of the traded ticker is by default the **closing price** of the next day. The consequence of the settling the pending orders is always the change of cash (loan) and stock (cash ↑ stock ↓ or cash ↓ stock ↑ ). Loans will result in interest costs which will accumulate every day the loan exists.

## Cache Mechanism

Since the backtester uses dataset that stores on disk memory and needs to access it frequently. To avoid repetitive File IO, the backtester uses a cache system to remember the market information of days that you have inspected before. By default, the cache can store 30 days of information, and you can change it depending on the memory size of your computer. For example, we recommend 30 for a 8-GB computer and 60 for a 16-GB one. 

```clojure
(CHANGE-CACHE-SIZE 60) ;; increase the cache size to 60
```

When cache exceeds its size limit, the backtester will remove the oldest information. The time here is determined by the date of the information rather than the adding date. If the data requested is not in cache, the backtester will fetch it from the file which will creates a delay. Therefore, we recommend you to refer back historical data within the range of the cache, which is 30. Older retrospect may take significantly longer time.

