{:title "User Guide"
:date "2021-01-20"
:layout :post
:tags []
:toc true}

## About the dataset

**[CRSP](https://connecthkuhk-my.sharepoint.com/:u:/g/personal/u35lyc_connect_hku_hk/ESQNElD-vL1KliKmOf57D2ABLCLYLHoc9wkJWhXUSTzCNw?e=DNaVWm)** is the official dataset for the backtester. New users are recommended to directly use this dataset to try out the functionalities of the backtester. Definitions for each column in CRSP can be found [here](https://crsp.org/files/CCM_Database_SAS_ASCII_R_FileFormats.pdf).

Of course, you can use your own dataset in the backtester. Follow this [guide]() on how to reform arbitrary dataset to be compatible with the backtester.

**[Compustat](https://connecthkuhk-my.sharepoint.com/:u:/g/personal/u35lyc_connect_hku_hk/Eddh3mmToBxCh7OlGx0R0-kB9G5a6Dq6xXW9dXXfHrn7OA?e=FQYfSl)** is a quarterly supplementary file of CRSP. It is used to contain company related information.

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

As introduced in the [time pointer](/posts/api#move-time-pointer), there is a global clock in the backtester. Your strategy will normally be run between two `next-date`s. 

![image](/img/trade-logic.jpg)

The most tricky part is the logic of trading. Say you are at date 2022-10-24, and you buy 10 shares of xx security. This will result in a pending order which will be settle by the backtester on the next call of `next-date` (if there is indeed xx security in the market). What you will observe is that your portfolio will not change immediately after `order`, and `order-record` will not be updated either. They will only be updated on next day. The price of the traded ticker is by default the **closing price** of the next day (`:PRC` in CRSP). 

## Cache Mechanism

Since the backtester uses dataset that stores on disk memory and needs to access it frequently. To avoid repetitive File IO, the backtester uses a cache system to remember the market information of days that you have inspected before. By default, the cache can store 30 days of information, and you can change it depending on the memory size of your computer. For example, we recommend 30 for a 8-GB computer and 60 for a 16-GB one. 

```clojure
(CHANGE-CACHE-SIZE 60) ;; increase the cache size to 60
```

When cache exceeds its size limit, the backtester will remove the oldest information. The time here is determined by the date of the information rather than the adding date. If the data requested is not in cache, the backtester will fetch it from the file which will creates a delay. Therefore, we recommend you to refer back historical data within the range of the cache, which is 30. Older retrospect may take significantly longer time.

