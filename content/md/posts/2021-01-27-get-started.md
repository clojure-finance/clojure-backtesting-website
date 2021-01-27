{:title "Get Started"
 :layout :post
 :toc true}

---

### Setting Up the Playground
<br>

1. Make sure that you have **leiningen** and **jupyter notebook** installed on your local machine. (These two may require further environment dependencies, please refer to their official websites for details.)

   - Installation guide for leiningen can be found at:

      <https://github.com/technomancy/leiningen/wiki/Packaging>

   - Jupyter notebook can be installed at: 

      <https://jupyter.org/install>

<br>

2. If you download this project for the **first time**, go to the repository root directory (i.e. `clojure-backtester/`) in the terminal and run

   `make init_clojupyter`

   *Note that this operation may download a number of required packages, which may take up some time.*

<br>

3. Run the following command so that the project could be compiled as a .jar file and added as a new Jupyter kernel:

   `make add_kernel`

   (If there is an error, make sure that you have **terminated** the Jupyter notebook running kernels before running this step.)

   <br>

   When the process is done, you should see the following output:

   ```
   Installed jar:      target/uberjar/clojure-backtesting-0.1.0-SNAPSHOT-standalone.jar
   Install directory:  ~/Library/Jupyter/kernels/backtesting_clojure
   Kernel identifier:  backtesting_clojure
         
   Installation successful.
   ```

<br>

4. Now when you restart the Jupyter Notebook application, you could select the kernel named `backtesting_clojure`. You can make use of the backtester by choosing this kernel.

<br>

### Beginner Tutorials
<br>

[This tutorial](http://kimh.github.io/clojure-by-example/#about) is really useful for beginners in Clojure to pick up basis quickly.

[This](https://clojuredocs.org/) is a more thorough documentation website for Clojure functions.

   ```
   #some demo code, to-add
   ```
