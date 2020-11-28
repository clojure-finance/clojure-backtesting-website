{:title "Get Started"
 :layout :post
 :toc true}

---

### Setting Up the Playground
<br>

1. Make sure there is **leiningen** and **jupyter notebook** installing on your local computer. (These two may require further environment dependencies, please refer to their official websites for details.)

   - Installation guide for leiningen can be found at:

      <https://github.com/technomancy/leiningen/wiki/Packaging>

   - Jupyter notebook can be installed at: 

      <https://jupyter.org/install>

<br>

2. If you download this project for the first time, go to the project directory and run

   `make init_clojupyter`

   *Note that this operation may download plenty of required packages, which may take up some time.*

<br>

3. Run the following command so that the project could be compiled as a .jar file and added as a new Jupyter kernel:

   `make add_kernel`

   When the process is done, you should see:

   ```
   Installed jar:      target/uberjar/clojure-backtesting-0.1.0-SNAPSHOT-standalone.jar
   Install directory:  ~/Library/Jupyter/kernels/backtesting_clojure
   Kernel identifier:  backtesting_clojure
         
   Installation successful.
   ```

<br>

4. Now when you open the Jupyter Notebook, you will see a kernel named `backtesting_clojure`. You can create new notebook using this kernel.

<br>

### Beginner Tutorial
<br>

   ```
   #some demo code, to-add
   ```
