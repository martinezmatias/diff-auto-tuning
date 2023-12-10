# DAT: Diff-Auto-Tuning

Repository of tool DAT presented in the following paper:

```bibtex
@article{martinez2023dat,
 title = {Hyperparameter Optimization for AST Differencing},
 journal = {IEEE Transactions on Software Engineering},
 year = {2023},
 doi = {10.1109/TSE.2023.3315935},
 author = {Matias Martinez and Jean-Rémy Falleri and Martin Monperrus},
 url = {http://arxiv.org/pdf/2011.10268},
}
```

## Goal of DAT

The goal of DAT to find a new configuration i.e., a particular value for each hyper-parameter of an AST differencing algorithm.

## How DAT works? 

DAT provides two types of optimization: `global` and `local`.

 

#### Global
In `global` optimization, the search of the best hyperparameters is done globally i.e., from a set of file-pairs.
DAT finds the values for the hyper-parameter(s) that produces the best edit scripts according to a fitness function.
The input of this optimization mode is a file containing a list of file pairs.
```
path to file A1, path to file A2
path to file B1, path to file B2
path to file C1, path to file C2
path to file D1, path to file D2
... 
```

In this mode, for each configuration `C` of the diff algorithm, DAT computes the fitness function on each file pair, then it computes a function (e.g., mean, median) to obtain the performance of `C` accross all the data.
Finally, it selects the configuration according with an objective goal (e.g., min, max).


#### Local 

The `local` optimization is done on a single file pair. 
DAT has as goal to search the hyperparameter that produces the best edit script according to a fitness function (by default shortest edit scripts)


## Usage


### Pre-requisites

To use TPE, it's requited to:
1) Install Python 3.x
2) Include Python in the path or create the environment variable `python.home` which value corresponds to the path to Python.
3) Install [hyperopt](http://hyperopt.github.io/hyperopt/) e.g., `pip install hyperopt`


### Commands


##### Mode Exhaustive and Local search  


```
fr.gumtree.autotuning.Main -left <path_to_file> -right <path_to_file> -mode exhaustive -scope local
```


##### Mode TPE and Local search 

```
fr.gumtree.autotuning.Main -left <path_to_file> -right <path_to_file> -mode tpe -scope local
```


##### Mode Exhaustive and Global search  

```
fr.gumtree.autotuning.Main -listpairs<path_to_file> -mode exhaustive -scope global
```


##### Mode TPE and Global search  

```
fr.gumtree.autotuning.Main -listpairs <path_to_file> -mode tpe -scope global
```



## Architecture


The interface `OptimizationMethod` provides the methods that define the two optimization types.

```
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception;


	public ResponseBestParameter computeBestLocal(File left, File right, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception;
```

As mentioned before, `computeBestGlobal` receives a file which stores a list of file-pairs, `computeBestLocal` two files, which corresponds to the file-pair to analyze.
Both methods has other two arguments: the fitness function, and the configuration of DAT for this execution (the type and number of configuration entries depend on the interface implementation). 


The current version of DAT provides two optimization methods, which implement the interface `OptimizationMethod`:

1) Exhaustive search
2) TPE 

Other optimization methods, not included in the current version of DAT, can be included in DAT by implementing this interface.


### Representation of configuration


For DAT, a configuration is a single point in the hyper-parameter space: for each hyper-parameter, a configuration has exactly one value.

The next listing shows the template for representing a particular configuration i.e., a point in the hyperparameter space:

```
<Algorithm Name>-<hyperparameter name 1>-<value of hyperparameter 1>-<hyperparameter name 2>-<value of hyperparameter 2>...-<hyperparameter name n>-<value of hyperparameter n>
```

For example, the following configuration:
```
ClassicGumtree-bu_minsim-0.1-bu_minsize-100-st_minprio-1-st_priocalc-size
```
concerns `ClassicGumtree` matching algorithm and assigns values 0.1 to hyper-parameter `bu_minsim`, 100 to `bu_minsize`, 1 to `bu_minsize` and  `size` value to `st_priocalc`. 
We recall that all those 4 are hyper-parameters of  ClassicGumtree matches.

Another diff algorithm may have different parameters.
For example `XY` has 3 hyper-parameters (`st_minprio`, `st_priocalc` and `xy_minsim`) and one configuration is:
```
XyMatcher-st_minprio-1-st_priocalc-size-xy_minsim-0.1
```


### DAT Output


All methods from `OptimizationMethod` returns a `ResponseBestParameter`  which contains:
1) a list of best hyper-parameters (DAT can find several sets of hyper-parameters that produces the same output).
2) the value of fitness function corresponding to the best configuration found.



# Contact:

Matias Martinez <matias.sebastian.martinez@gmail.com>





