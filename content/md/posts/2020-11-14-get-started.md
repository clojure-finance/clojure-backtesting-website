# Get Started

## Default playground: Jupyter Notebook

1. Make sure there is lein and jupyter notebook installing on your local computer. (These two may require further environment dependencies, please refer to official websites for details.)

   1. Installation guide for lein can be found:

      https://github.com/technomancy/leiningen/wiki/Packaging.

      Jupyter notebook can be installed using: 

      https://jupyter.org/install

2. If you download this project for the first time, go to the project derectory and run

   `make init_clojupyter`

   This operation may download plenty of things needed, which will take up some time.

3. Compile the project as a .jar file and add it as a new Jupyter kernel by:

   `make add_kernel`

   (Jupyter notebook should be shut down before this step)

   When the process is done, you should see

       Installed jar:      target/uberjar/clojure-backtesting-0.1.0-SNAPSHOT-standalone.jar
       Install directory:  ~/Library/Jupyter/kernels/backtesting_clojure
       Kernel identifier:  backtesting_clojure
       
       Installation successful.

4. Now when you open the Jupyter Notebook, you will see a kernel named `backtesting_clojure`. You can create new notebook using this kernel.