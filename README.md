# DAT: Diff-Auto-Tuning

Repository of tool DAT presented in the following paper:

```
@misc{dat,
  author = {Martinez, Matias and Falleri, Jean-Rémy and Monperrus, Martin},
  
  title = {Hyperparameter Optimization for AST Differencing},
  
  publisher = {arXiv},
  
  year = {2020},
  
  doi = {10.48550/ARXIV.2011.10268},
  
  url = {https://arxiv.org/abs/2011.10268},
   
  keywords = {Software Engineering (cs.SE), FOS: Computer and information sciences, FOS: Computer and information sciences},
}

```

## Usage


UNDER PROGRESS

### Optimization scope


The interface `OptimizationMethod` defines two main optimizations scopes: `global` and `local` optimization.

#### Global
In `global` optimization, the search of the best hyperparameters is done globally i.e., from a set of file-pairs.
It finds the hyperparameter that produce the best edit scripts according to a fitness function.
The input of this mode is a file with a list of file pairs.
```
path to file A1, path to  file A2
path to file B1, path to file B2
path to file C1, path to file C2
path to file D1, path to file D2
... 
```


#### Local 

On the contrary, the `local` optimization is done on a single file pair and has as goal to search the hyperparameter that produces the best edit script according to a fitness function (by default shortest edit scripts)


### Optimization Method

The interface `OptimizationMethod` should be implemented by particular optimization methods.
The current version of DAT provides two optimization methods:
1) Exhaustive search
2) TPE 

Other optimization methods, not included in the current version of DAT, can be included in DAT by implementing this interface.


### Representation of configuration

It's important to remark how DAT represents a particular configuration of a diff algorithm.
A configuration is a point in the hyperparameter space: for each hyperparameter, the configuration has exactly one value.

The next listing shows the template for representing a particular configuration i.e., a point in the hyperparameter space:

```
<Algorithm Name>-<hyperparameter name 1>-<value of hyperparameter 1>-<hyperparameter name 2>-<value of hyperparameter 2>...-<hyperparameter name n>-<value of hyperparameter n>
```

For example, the following configuration:
```
ClassicGumtree-bu_minsim-0.1-bu_minsize-100-st_minprio-1-st_priocalc-size
```
concerns `ClassicGumtree` matching algorithm and assigns values 0.1 to hyperparameter `bu_minsim`, 100 to `bu_minsize`, 1 to `bu_minsize` and  `size` value to `st_priocalc`. 
We recall that all those 4 are hyperparameters of  ClassicGumtree matches.

Another diff algorithm may have different parameters.
For example `XY` has 3 hyperparameters (`st_minprio`, `st_priocalc` and `xy_minsim`) and one configuration is:
```
XyMatcher-st_minprio-1-st_priocalc-size-xy_minsim-0.1
```




### Output




All methods from `OptimizationMethod` returns a `ResponseBestParameter`  which contains a list of best hyperparameters: DAT can find several sets of hyperparameters that produces the same output.


For each file-pair analyzed P, DAT gives the possibility to save on disk files with different information.
First, it saves a give `summary_cases_<pair_identifier>`, which is a CSV file were each row is a particular configuration C and there is a row that indicates the size of the edit script  by running a diff algorithm on P configured by C.
Similarly, the file `result_size_per_config__<pair_identifier>` stores the edit script size for each configuration executed on P.

Second, it saves the edit script generated for each particular hyperparameter optimization (i.e., each point in the space of hyperconfigurations).
DAT saves only distinct edit scripts to save storage space.





