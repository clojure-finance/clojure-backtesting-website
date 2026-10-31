;; Draws content/img/value-strategy.svg, the home page's second chart: the
;; monthly earnings-yield strategy of the value_strategy worksheet against
;; the IDX index, on the library's synthetic Yahoo snapshots.
;;
;; Run it from a clone of the library, next to this repository:
;;
;;   cd ../clojure-backtesting
;;   lein run -m clojure.main ../clojure-backtesting-website/dev/value_strategy_chart.clj
;;
;; It prints the figures the home page quotes, so they can be checked
;; against content/md/pages/index.md.

(ns value-strategy-chart
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure-backtesting.counter :refer [get-date]]
            [clojure-backtesting.data-management :refer [get-permno-by-key]]
            [clojure-backtesting.evaluate :refer [benchmark-stats update-eval-report]]
            [clojure-backtesting.fundamentals :refer [earnings-yield]]
            [clojure-backtesting.order :refer [end-order next-date]]
            [clojure-backtesting.parameters :refer [update-transaction-cost]]
            [clojure-backtesting.portfolio :refer [eval-report-data init-portfolio order-record portfolio-value]]
            [clojure-backtesting.strategy :refer [new-period? top-n rebalance equal-weights]]
            [clojure-backtesting.yahoo :refer [build-yahoo-dataset! load-yahoo-dataset]]))

(def out-file "../clojure-backtesting-website/content/img/value-strategy.svg")

;; ============ The run ============

(build-yahoo-dataset! "resources/sample-yahoo" :out-dir "target/sample-yahoo")
(load-yahoo-dataset "target/sample-yahoo")
(update-transaction-cost 0.001)
(init-portfolio "2023-01-03" 100000)

(def picks (atom []))

(while (neg? (compare (get-date) "2024-12-31"))
  (when (new-period? :month)
    (let [top (top-n earnings-yield 2 :universe ["ALFA" "BETA" "GAMA" "DLTA"])]
      (swap! picks conj {:date (get-date) :picks top})
      (rebalance (equal-weights top))))
  (next-date)
  (update-eval-report))
(end-order)
(update-transaction-cost 0.0)

(def values (mapv (juxt :date :tot-value) (deref portfolio-value)))
(def idx0 (get-permno-by-key (ffirst values) "IDX" :APRC))
(def index-values (mapv (fn [[d _]] [d (* 100000.0 (/ (get-permno-by-key d "IDX" :APRC) idx0))]) values))
(def stats (benchmark-stats "IDX"))

(println "Final value:" (second (peek values)))
(println "Index, scaled:" (second (peek index-values)))
(println "Last report row:" (last (deref eval-report-data)))
(println "Benchmark stats:" (into (sorted-map) stats))
(println "Orders:" (count (deref order-record)))
(println "Pick changes:" (map first (partition-by :picks (deref picks))))

;; ============ The chart ============

(def width 760)
(def height 372)
(def left 64)
(def right 668)
(def top 56)
(def bottom 290)
(def strip-y 314)

(def n (count values))
(defn x-of [i] (+ left (* (- right left) (/ (double i) (dec n)))))

(def y-min 80000.0)
(def y-max 180000.0)
(defn y-of [v] (- bottom (* (- bottom top) (/ (- v y-min) (- y-max y-min)))))

(defn fmt [x] (format "%.1f" (double x)))

(defn path [series]
  (str "M" (str/join " L" (map-indexed (fn [i [_ v]] (str (fmt (x-of i)) " " (fmt (y-of v)))) series))))

;; truncated like the evaluation report's :tot-value
(defn money [v] (str "$" (format "%,d" (long v))))

(def date-index (into {} (map-indexed (fn [i [d _]] [d i]) values)))

(def month-names ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])

(def x-ticks
  ;; the first trading day of every quarter
  (->> values
       (map first)
       (partition-by #(subs % 0 7))
       (map first)
       (filter #(#{"01" "04" "07" "10"} (subs % 5 7)))))

(def segments
  ;; [start-index end-index label] for each run of identical picks
  (let [changes (map first (partition-by :picks (deref picks)))
        starts (mapv #(date-index (:date %)) changes)
        ends (conj (subvec starts 1) (dec n))]
    (mapv (fn [c s e] [s e (str/join " + " (:picks c))]) changes starts ends)))

(def colors {"ALFA + GAMA" "#2f6fdb" "ALFA + BETA" "#16a34a"})

(def svg
  (str/join
   "\n"
   (concat
    [(str "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " width " " height "\" width=\"" width "\" height=\"" height "\""
          " font-family=\"Inter, -apple-system, 'Segoe UI', Helvetica, Arial, sans-serif\" role=\"img\" aria-labelledby=\"t d\">")
     "<title id=\"t\">Value strategy against the index</title>"
     (str "<desc id=\"d\">Portfolio value of a monthly earnings-yield strategy on the synthetic Yahoo sample from January 2023 to December 2024, ending at "
          (money (second (peek values))) ", against the IDX index scaled to the same start, ending at " (money (second (peek index-values)))
          ". A strip below shows which two securities were held.</desc>")
     (str "<rect width=\"" width "\" height=\"" height "\" fill=\"#ffffff\"/>")
     ;; legend
     "<g transform=\"translate(64,22)\"><line x1=\"0\" y1=\"0\" x2=\"16\" y2=\"0\" stroke=\"#2f6fdb\" stroke-width=\"2.5\"/><text x=\"22\" y=\"4\" font-size=\"12\" fill=\"#334155\">Value strategy (top 2 by earnings yield, monthly)</text></g>"
     "<g transform=\"translate(400,22)\"><line x1=\"0\" y1=\"0\" x2=\"16\" y2=\"0\" stroke=\"#64748b\" stroke-width=\"2\" stroke-dasharray=\"5 4\"/><text x=\"22\" y=\"4\" font-size=\"12\" fill=\"#334155\">IDX index, same starting value</text></g>"]
    ;; grid and y labels
    (for [v (range 80000 180001 20000)
          :let [y (fmt (y-of v))]]
      (str "<line x1=\"" left "\" y1=\"" y "\" x2=\"" right "\" y2=\"" y "\" stroke=\"#e5e9f0\"/>"
           "<text x=\"" (- left 8) "\" y=\"" (+ (Double/parseDouble y) 4) "\" font-size=\"11\" fill=\"#64748b\" text-anchor=\"end\">$" (quot v 1000) "k</text>"))
    ;; x labels
    (for [d x-ticks
          :let [x (fmt (x-of (date-index d)))
                label (str (month-names (dec (Integer/parseInt (subs d 5 7)))) " " (subs d 0 4))]]
      (str "<line x1=\"" x "\" y1=\"" bottom "\" x2=\"" x "\" y2=\"" (+ bottom 5) "\" stroke=\"#cfd8e3\"/>"
           "<text x=\"" x "\" y=\"" (+ bottom 18) "\" font-size=\"11\" fill=\"#64748b\" text-anchor=\"middle\">" label "</text>"))
    [;; the start line
     (str "<line x1=\"" left "\" y1=\"" (fmt (y-of 100000)) "\" x2=\"" right "\" y2=\"" (fmt (y-of 100000)) "\" stroke=\"#cfd8e3\" stroke-dasharray=\"2 3\"/>")
     (str "<path d=\"" (path index-values) "\" fill=\"none\" stroke=\"#64748b\" stroke-width=\"1.6\" stroke-dasharray=\"5 4\"/>")
     (str "<path d=\"" (path values) "\" fill=\"none\" stroke=\"#2f6fdb\" stroke-width=\"2.2\" stroke-linejoin=\"round\"/>")
     ;; end labels
     (str "<text x=\"" (+ right 8) "\" y=\"" (fmt (+ (y-of (second (peek values))) 4)) "\" font-size=\"12\" font-weight=\"600\" fill=\"#1d4fb8\">"
          (money (second (peek values))) "</text>")
     (str "<text x=\"" (+ right 8) "\" y=\"" (fmt (+ (y-of (second (peek index-values))) 4)) "\" font-size=\"12\" font-weight=\"600\" fill=\"#475569\">"
          (money (second (peek index-values))) "</text>")
     ;; holdings strip
     (str "<text x=\"" (- left 8) "\" y=\"" (+ strip-y 15) "\" font-size=\"11\" fill=\"#64748b\" text-anchor=\"end\">Held</text>")]
    (for [[s e label] segments
          :let [x0 (x-of s) x1 (x-of e) w (- x1 x0)]]
      (str "<rect x=\"" (fmt x0) "\" y=\"" strip-y "\" width=\"" (fmt w) "\" height=\"22\" fill=\"" (colors label "#94a3b8") "\" fill-opacity=\"0.85\"/>"
           (when (> w 90)
             (str "<text x=\"" (fmt (+ x0 (/ w 2))) "\" y=\"" (+ strip-y 15) "\" font-size=\"11\" font-weight=\"600\" fill=\"#ffffff\" text-anchor=\"middle\">" label "</text>"))))
    [(str "<text x=\"" left "\" y=\"" (+ strip-y 46) "\" font-size=\"11\" fill=\"#64748b\">"
          "Rebalanced on the first trading day of each month, with a 0.1% commission on every trade.</text>")
     "</svg>"])))

(io/make-parents out-file)
(spit out-file (str svg "\n"))
(println "Wrote" out-file)
(shutdown-agents)
