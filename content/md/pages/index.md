{:title "Welcome to the Clojure Backtesting Library"
 :layout :page
 :page-index 0
 :navbar? true
 :home? true}

## Introduction

Welcome to the Clojure Backtesting Library! This is an open-source library for quantitative finance and trading developed at the Faculty of Business and Economics ("HKU Business School") of the University of Hong Kong (HKU). 

## Features

- **Support for large data files**  
  The library has been tested with data up to 30 GB large.

- **All native types**  
  All the datatypes used to store data is native Clojure (or Java) types!  

- **Lazy operations**  
  Some operations will not be executed immediately. Dataframe will intelligently pipeline the operations altogether in computation.  

- **Built-in indicators**  
  Common technial indicators such as Relative Strength Index (RSI) and Exponential Moving Average (EMA) are integrated within the library.

- **Write your unique trading strategy**  
  Making use of the variety of APIs, you could customise your own strategy and enjoy the advantages of programming in a functional language at the same time. 

<br>

## Table of Contents

### Part I: Installation & Pre-requisites

- [Get Started](/posts/get-started)
- [Preliminaries](/posts/preliminaries)

### Part II: API documentation

- [Data Management](/posts/data-management)
- [Counter](/posts/counter)
- [Parameters](/posts/parameters)
- [Order](/posts/order)
- [Portfolio](/posts/portfolio)
- [Evaluate](/posts/evaluate)
- [Large Data](/posts/large-data)

### Part III: Examples

- [Golden Cross](/posts/example-golden-cross)
- [Fundamental Analysis](/posts/example-fundamental-analysis)
- [Buying on Margin](/posts/example-buying-on-margin)
