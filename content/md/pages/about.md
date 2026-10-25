{:title "Why Clojure?"
 :layout :page
 :page-index 1
 :navbar? true
 :section "Introduction"
 :lead "Why a backtester in Clojure, who builds it, and how to get involved."}

Clojure Backtesting is an open-source library for testing trading strategies on historical daily data, developed at HKU Business School as part of the [clojure-finance](https://github.com/clojure-finance) projects. The source code is on [GitHub](https://github.com/clojure-finance/clojure-backtesting) under the MIT License.

## Why Clojure

Clojure is a functional programming language that runs on the Java Virtual Machine. Several of its features suit financial modelling and data-heavy research:

- **Immutable data.** Values do not change under you, which makes a long simulation easier to reason about and to test.
- **The REPL.** You build a strategy interactively, one expression at a time, and inspect the portfolio after every step.
- **The JVM.** Clojure code can call any Java library, and runs at the speed of compiled Java.
- **Built for concurrency.** Immutable data and Clojure's reference types make parallel work safer and simpler.
- **Lazy sequences.** Data can be processed as it is read, which is how the backtester handles datasets larger than memory.

Functional languages have a long history in finance: Jane Street, for example, uses [OCaml](https://blog.janestreet.com/why-ocaml/) as its primary language, and many banks use Scala. We believe Clojure has a lot to offer here too, and this library is one of the resources we are building for the community.

## Report bugs and contribute

The library is under active development. If you find a bug or a mistake in these pages, please [open an issue](https://github.com/clojure-finance/clojure-backtesting/issues). Ideas for new examples are welcome too.
