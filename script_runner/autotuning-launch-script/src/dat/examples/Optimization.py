import hyperopt

#http://hyperopt.github.io/hyperopt/
#https://blog.dominodatalab.com/hyperopt-bayesian-hyperparameter-optimization/

# define an objective function
def objective(args):
    case, val = args
    if case == 'case 1':
        return val
    else:
        return val ** 2

# define a search space
from hyperopt import hp
space = hp.choice('a',
    [
        ('case 1', 1 + hp.lognormal('c1', 0, 1)),
        ('case 2', hp.uniform('c2', -10, 10))
    ])
###http://hyperopt.github.io/hyperopt/
# minimize the objective over the space
from hyperopt import fmin, tpe
best = fmin(objective, space, algo=tpe.suggest, max_evals=100)

print(best)
# -> {'a': 1, 'c2': 0.01420615366247227}
print(hyperopt.space_eval(space, best))
# -> ('case 2', 0.01420615366247227}

#https://scikit-learn.org/stable/modules/cross_validation.html
#https://scikit-learn.org/stable/modules/grid_search.html#grid-search