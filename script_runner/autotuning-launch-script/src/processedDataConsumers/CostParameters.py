##From GridSearch
#range = [0.0001, 0.00025, 0.0005, 0.00075, 0.001, 0.0025, 0.005, 0.0075, 0.01, 0.025, 0.05,0.075, 0.1, 0.25, 0.5, 0.75,  1]
##From TPE RQ5
#ratio = [0.0001, 0.00025, 0.0005, 0.00075, 0.001, 0.0025, 0.005, 0.0075, 0.01, 0.025, 0.05,0.075, 0.1, 0.25, 0.5, 0.75,  1]
## RQ TPE 1
#ratio = [0.0001, 0.00025, 0.0005, 0.001, 0.0025, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1]

ratioDataset = [0.0001, 0.0005, 0.001,  0.005, 0.01, 0.05, 0.1, 0.5, 1] #[0.0001, 0.00025, 0.0005, 0.001, 0.0025, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1]

evals_range = [10, 50, 100, 500, 1000]#[10, 20, 50, 100, 200, 500, 1000]

OVERWRITE_RESULTS = False

SEEDS_TO_EXECUTE=5

KFOLD_VALUE = 10